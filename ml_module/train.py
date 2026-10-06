import pandas as pd
import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
import os

# Load the updated legal dataset
dataset_path = os.path.join(os.path.dirname(__file__), 'legal_dataset.csv')
if not os.path.exists(dataset_path):
    print(f"Error: {dataset_path} not found.")
    exit(1)

df = pd.read_csv(dataset_path)

# Prepare the data
# We train on 'text' and 'description' (which acts as the crime category)
X = df['text']
y = df['description']

# Split the data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Create a pipeline: Vectorizer -> Classifier
model = Pipeline([
    ('tfidf', TfidfVectorizer(ngram_range=(1, 2), stop_words='english')),
    ('clf', LogisticRegression(C=1.0, solver='liblinear', multi_class='auto'))
])

# Train the model
print("Training the model on Legal (IPC + BNS) dataset...")
model.fit(X_train, y_train)

# Save the model
model_file = os.path.join(os.path.dirname(__file__), 'legal_model.joblib')
joblib.dump(model, model_file)
print(f"Model saved to {model_file}")

# Save the multi-law mapping
# description -> {ipc, bns}
law_mapping = df.drop_duplicates('description').set_index('description')[['ipc_label', 'bns_label']].to_dict('index')
joblib.dump(law_mapping, os.path.join(os.path.dirname(__file__), 'law_mapping.joblib'))
print("Legal mapping (IPC & BNS) saved.")
