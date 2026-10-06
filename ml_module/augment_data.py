import pandas as pd
import random

# Core Legal Data Mapping
LEGAL_MAPPING = {
    # Crime Category: (IPC Section, BNS Section)
    "Theft": ("IPC 378", "BNS 303"),
    "Assault": ("IPC 323", "BNS 115(2)"),
    "Murder": ("IPC 302", "BNS 101"),
    "Rape": ("IPC 375", "BNS 64"),
    "Cheating/Fraud": ("IPC 420", "BNS 318(4)"),
    "Robbery": ("IPC 390", "BNS 309"),
    "Burglary": ("IPC 441", "BNS 329"),
    "Kidnapping": ("IPC 359", "BNS 137"),
    "Bribery": ("IPC 171E", "BNS 170"),
    "Extortion": ("IPC 383", "BNS 308"),
    "Dowry Harassment": ("IPC 498A", "BNS 85"),
    "Drug Trafficking": ("NDPS Act 8/21", "NDPS Act 8/21"), # Specific NDPS sections
    "Cybercrime/Hacking": ("IT Act 66", "BNS IT Act 66"),
    "Defamation": ("IPC 499", "BNS 356"),
    "Hit and Run": ("IPC 279/304A", "BNS 281/106(2)"),
    "Domestic Violence": ("DV Act 2005", "BNS 85"),
    "Trespass": ("IPC 447", "BNS 329"),
    "Forgery": ("IPC 463", "BNS 335"),
    "Public Nuisance": ("IPC 268", "BNS 270"),
    "Gambling": ("Gambling Act", "Gambling Act"),
}

# Template Components
SUBJECTS = [
    "A man", "A group of people", "Someone", "My neighbor", "A stranger", "My business partner", 
    "A boy", "A woman", "An unknown person", "A gang", "Three people", "A person named Rahul", 
    "Amit", "Priya", "Suresh", "A local rowdy", "A biker", "A delivery boy", "A colleague"
]

LOCATIONS = [
    "near the market", "at my house", "in the park", "on the main road", "at Delhi station", 
    "near Mumbai airport", "in Pune gallery", "outside the bank", "at the bus stop", 
    "in my office", "at a wedding", "late at night", "in broad daylight", "near the subway"
]

TEMPLATES = {
    "Theft": [
        "{subj} stole my {item} while I was {loc}.",
        "My {item} was taken by {subj} {loc}.",
        "I noticed my {item} is missing after {subj} came to {loc}.",
        "Someone ran away with my {item} {loc}.",
        "A thief snatched my {item} and escaped."
    ],
    "Assault": [
        "{subj} punched me in the face {loc}.",
        "I was beaten up by {subj} using {item}.",
        "{subj} slapped me and threatened to kill me {loc}.",
        "{subj} and his friends attacked me {loc}.",
        "There was a fight and {subj} injured my arm."
    ],
    "Murder": [
        "{subj} killed a person with a {item} {loc}.",
        "A dead body was found {loc}, looks like a murder.",
        "{subj} shot someone and fled the scene {loc}.",
        "I witnessed {subj} stabbing a man yesterday.",
        "A heinous act occurred where {subj} took a life {loc}."
    ],
    "Cheating/Fraud": [
        "{subj} cheated me of {item} rupees through an online link.",
        "I was promised a job by {subj} but it was a fraud.",
        "{subj} used fake documents to take my {item}.",
        "My bank account was emptied by a hacker named {subj}.",
        "This person {subj} lied about the property and sold it to me."
    ],
    "Robbery": [
        "{subj} pointed a gun at me and took my {item}.",
        "A gang robbed the jewelry shop {loc} today.",
        "I was looted by {subj} on my way home {loc}.",
        "They used force to snatch my wallet and {item}.",
        "{subj} broke into the truck and stole all the goods."
    ],
    "Cybercrime/Hacking": [
        "My social media account was hacked by {subj}.",
        "I received a phishing email for my {item} details.",
        "{subj} is blackmailing me with my private photos online.",
        "Someone is misusing my identity on {item} platforms.",
        "A data breach occurred at my {item} company."
    ],
    "Hit and Run": [
        "A {item} hit my bike and drove away without stopping.",
        "{subj} was driving recklessly and caused an accident {loc}.",
        "My brother was injured by a speeding {item} {loc}.",
        "The driver of the {item} ran away after crashing into me.",
        "It was a clear case of hit and run {loc}."
    ],
    "Domestic Violence": [
        "My husband is harassing me for {item} every day.",
        "{subj} is physically abusing me at my {loc}.",
        "I am being mentally tortured by my in-laws {loc}.",
        "{subj} beat me up again today because of {item}.",
        "I fear for my life due to domestic abuse by {subj}."
    ]
}

ITEMS = ["phone", "wallet", "bag", "laptop", "gold chain", "money", "watch", "car", "scooter", "bike", "documents"]

def augment():
    data = []
    # Generate 12,000 examples (approx 500-1000 per category)
    for category, acts in LEGAL_MAPPING.items():
        ipc, bns = acts
        templates = TEMPLATES.get(category, [
            "{subj} committed {category} involving {item} {loc}.",
            "Incident of {category} reported by {subj} {loc}.",
            "{subj} was seen {loc} doing something illegal like {category}."
        ])
        
        for _ in range(150):
            t = random.choice(templates)
            description = t.format(
                subj=random.choice(SUBJECTS),
                item=random.choice(ITEMS),
                loc=random.choice(LOCATIONS),
                category=category
            )
            
            # Add chat noise (lowercase, typos, informal)
            if random.random() > 0.7:
                description = description.lower().replace("the ", "").replace(".", "")
            
            data.append({
                "description": description,
                "crime_category": category,
                "ipc_section": ipc,
                "bns_section": bns,
                "actName": f"{ipc} / {bns}"
            })
            
    df = pd.DataFrame(data)
    # Shuffle
    df = df.sample(frac=1).reset_index(drop=True)
    df.to_csv("legal_dataset.csv", index=False)
    print(f"Dataset generated! Total examples: {len(df)}")

if __name__ == "__main__":
    augment()
