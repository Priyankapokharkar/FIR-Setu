from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import joblib
import os
import pickle

app = Flask(__name__)
CORS(app)

# Load the Transformer model, tokenizer, and law mappings
model_dir = os.path.join(os.path.dirname(__file__), 'legal_transformer_model')
metadata_path = os.path.join(os.path.dirname(__file__), 'model_metadata.pkl')
legacy_label_map_path = os.path.join(os.path.dirname(__file__), 'label_map.pt')
legacy_law_mapping_path = os.path.join(os.path.dirname(__file__), 'law_mapping.joblib')

# Global variables for model components
tokenizer = None
model = None
label_map = {}
section_map = {}

def load_model():
    global tokenizer, model, label_map, section_map
    print(f"--- STARTING LEGAL AI SERVICE ---")
    
    # 1. Determine Model Path
    model_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), 'legal_transformer_model'))
    print(f"[*] Checking for Transformer Model at: {model_dir}")
    
    if os.path.exists(model_dir):
        try:
            print("[*] Found model folder. Loading components...")
            tokenizer = AutoTokenizer.from_pretrained(model_dir)
            model = AutoModelForSequenceClassification.from_pretrained(model_dir)
            model.eval()
            print("[+] Transformer Model loaded successfully.")
        except Exception as e:
            print(f"[!] Error loading model from {model_dir}: {str(e)}")
            model = None
    else:
        print(f"[!] WARNING: Folder NOT FOUND! Ensure the 250MB model files are present in: {model_dir}")

    # 2. Metadata Loading
    metadata_path = os.path.join(os.path.dirname(__file__), 'model_metadata.pkl')
    legacy_label_map_path = os.path.join(os.path.dirname(__file__), 'label_map.pt')
    legacy_law_mapping_path = os.path.join(os.path.dirname(__file__), 'law_mapping.joblib')

    if os.path.exists(metadata_path):
        with open(metadata_path, 'rb') as f:
            meta = pickle.load(f)
            label_map = meta.get('label_map', {})
            section_map = meta.get('section_map', {})
        print("[+] New metadata loaded successfully.")
    # Fallback to Legacy System
    elif os.path.exists(legacy_label_map_path) and os.path.exists(legacy_law_mapping_path):
        label_map = torch.load(legacy_label_map_path)
        section_map = joblib.load(legacy_law_mapping_path)
        print("[+] Legacy mappings loaded successful.")
    else:
        print("[!] Error: No legal mappings found (model_metadata.pkl missing).")

load_model()

@app.route('/', methods=['GET'])
def home():
    return jsonify({'success': True, 'message': 'Local Legal AI is active!'})

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        if not data or 'text' not in data:
            return jsonify({'success': False, 'message': 'No text provided'}), 400
        
        text = data['text'].strip().lower()

        # --- 1. CONVERSATIONAL INTENT CHECK (GREETINGS) ---
        greetings = {
            'hi': "Hello! I am your LawyerApp Legal Assistant. How can I help you today? You can describe an incident (e.g., 'My bike was stolen') to get legal sections.",
            'hello': "Hi there! I'm here to assist with legal classification and FIR drafting. What happened?",
            'hey': "Hey! Need help with an FIR or identifying legal sections? Just describe the incident.",
            'help': "I can help you identify IPC/BNS sections based on an incident. Just tell me what occurred.",
            'who are you': "I am the LawyerApp AI, a locally-hosted transformer designed to provide expert legal guidance for IPC and BNS laws.",
            'good morning': "Good morning! How can I assist you with legal filing today?",
            'good afternoon': "Good afternoon! Ready to help you with any legal classification or FIR drafting.",
            'good evening': "Good evening! I am here to provide legal advice and incident classification."
        }


        if text in greetings:
            return jsonify({
                'success': True,
                'is_greeting': True,
                'reply': greetings[text]
            })

        # --- 2. LEGAL CLASSIFICATION (TRANSFORMER) ---
        if model is None or tokenizer is None:
            return jsonify({'success': False, 'message': 'Model not loaded on server'}), 500

        # Tokenize input
        inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True, max_length=128)
        
        # Run Inference
        with torch.no_grad():
            outputs = model(**inputs)
            logits = outputs.logits
            prediction_idx = torch.argmax(logits, dim=1).item()
        
        # Map Prediction
        category = label_map.get(prediction_idx, "General Incident")
        
        # Get Law Details (Handling both New and Legacy formats)
        details = section_map.get(category, {})
        
        # Normalize keys (Legacy used 'ipc_label', New uses 'ipc_section')
        ipc = details.get('ipc_section') or f"IPC {details.get('ipc_label', '')}" 
        bns = details.get('bns_section') or f"BNS {details.get('bns_label', '')}"
        act_name = details.get('actName') or f"{ipc} / {bns}"

        return jsonify({
            'success': True,
            'prediction': {
                'crime_category': category,
                'ipc_section': ipc,
                'bns_section': bns,
                'actName': act_name
            }
        })
    except Exception as e:
        print(f"Prediction Error: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 500

if __name__ == '__main__':
    app.run(port=8000, debug=False) # Debug false to avoid double loading on CPU
