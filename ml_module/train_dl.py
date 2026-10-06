import pandas as pd
import torch
import numpy as np
from datasets import Dataset
from transformers import AutoTokenizer, AutoModelForSequenceClassification, TrainingArguments, Trainer
from sklearn.preprocessing import LabelEncoder
import os
import pickle

# 1. Load the optimized dataset
dataset_path = 'legal_dataset.csv'
if not os.path.exists(dataset_path):
    print(f"Error: {dataset_path} not found. Run augment_data.py first.")
    exit(1)

df = pd.read_csv(dataset_path).dropna()

# 2. Encode Labels (CRIME CATEGORY)
label_encoder = LabelEncoder()
df['labels'] = label_encoder.fit_transform(df['crime_category'])
num_labels = len(label_encoder.classes_)

# Save label mapping for inference
label_map = {idx: label for idx, label in enumerate(label_encoder.classes_)}
section_map = df.drop_duplicates('crime_category').set_index('crime_category')[['ipc_section', 'bns_section', 'actName']].to_dict('index')

with open('model_metadata.pkl', 'wb') as f:
    pickle.dump({'label_map': label_map, 'section_map': section_map}, f)

# 3. Model and Tokenizer
model_name = "distilbert-base-uncased"
tokenizer = AutoTokenizer.from_pretrained(model_name)

# 4. Prepare Dataset (80/20 split)
train_df = df.sample(frac=0.8, random_state=42)
test_df = df.drop(train_df.index)

train_dataset = Dataset.from_pandas(train_df)
test_dataset = Dataset.from_pandas(test_df)

def tokenize_function(examples):
    # Reduced max_length to 64 for 2x speedup (most FIR descriptions are brief)
    return tokenizer(examples["description"], padding="max_length", truncation=True, max_length=64)

tokenized_train = train_dataset.map(tokenize_function, batched=True)
tokenized_test = test_dataset.map(tokenize_function, batched=True)

# 5. Training Arguments (Speed + Accuracy balance)
training_args = TrainingArguments(
    output_dir="./results",
    num_train_epochs=12,              # High epochs for 99% accuracy on small dataset
    per_device_train_batch_size=16,   # Larger batch for speed
    per_device_eval_batch_size=16,
    warmup_steps=300,
    weight_decay=0.01,
    logging_dir="./logs",
    logging_steps=50,
    eval_strategy="epoch",
    save_strategy="epoch",
    load_best_model_at_end=True,
    metric_for_best_model="accuracy"
)

def compute_metrics(eval_pred):
    logits, labels = eval_pred
    predictions = np.argmax(logits, axis=-1)
    return {"accuracy": (predictions == labels).mean()}

# 6. Initialize Trainer
model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels=num_labels)

# Move to GPU if available
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_train,
    eval_dataset=tokenized_test,
    compute_metrics=compute_metrics
)

# 7. Train and Save
print(f"Starting optimized training (3,000 examples, 12 epochs) on {device}...")
trainer.train()

print("Evaluating final model accuracy...")
metrics = trainer.evaluate()
print(f"Final Validation Accuracy: {metrics['eval_accuracy'] * 100:.2f}%")

# Save the model
model.save_pretrained("./legal_transformer_model")
tokenizer.save_pretrained("./legal_transformer_model")
print("High-Accuracy Model saved to ./legal_transformer_model")
