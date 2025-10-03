from google import genai
from google.genai import types
import json

client = genai.Client()

def get_next_steps(patient_data, risk_score, reliability_score):
    all_fields = [
        "Age", "Gender", "Ethnicity", "EducationLevel", "BMI", "Smoking", "AlcoholConsumption",
        "PhysicalActivity", "DietQuality", "SleepQuality", "FamilyHistoryAlzheimers",
        "CardiovascularDisease", "Diabetes", "Depression", "HeadInjury", "Hypertension",
        "SystolicBP", "DiastolicBP", "CholesterolTotal", "CholesterolLDL", "CholesterolHDL",
        "CholesterolTriglycerides", "MMSE", "FunctionalAssessment", "MemoryComplaints",
        "BehavioralProblems", "ADL", "Confusion", "Disorientation", "PersonalityChanges",
        "DifficultyCompletingTasks", "Forgetfulness"
    ]
    missing_fields = [f for f in all_fields if f not in patient_data]

    prompt = (
    "Given the following patient data and risk assessment, "
    "summarize the results and offer supportive advice. "
    "This is the final screen of the app. "
    "Do NOT suggest any further in-app tests, quizzes, or checks, as these are not available. "
    "You may suggest steps the patient can take on their own, such as lifestyle changes, talking to loved ones, or seeing a healthcare professional if concerned. "
    "Reply like you are talking to the patient, use comforting non-technical language. "
    "Keep it very brief and absolutely to the point, since the patient will not read very far.\n\n"
    f"Patient data (only fields provided by the user):\n{json.dumps(patient_data, indent=2)}\n"
    f"Risk score: {risk_score}\n"
    f"Reliability score: {reliability_score}\n"
    )
    if missing_fields:
        prompt += (
            "If given limited information, make sure the patient knows they need to fill "
            "more of the form, as in it is limited information on *their* side, but don't make it too lengthy."
        )
    prompt += (
        "These are all the questions the patient is asked:"
        "- What is your current age? (60-90)"
        "- What is your gender?"
        "- What is your ethnicity?"
        "- What is your highest level of education?"
        "- What is your Body Mass Index (BMI)? (15-40, e.g., 24.5)"
        "- Do you currently smoke?"
        "- How many units of alcohol do you consume weekly? (0-20 units)"
        "- How many hours of physical activity do you engage in weekly? (0-10 hours)"
        "- On a scale of 0 to 10, how would you rate your diet quality?"
        "- On a scale of 4 to 10, how would you rate your sleep quality?"
        "- Do you have a family history of Alzheimer's Disease?"
        "- Have you been diagnosed with cardiovascular disease?"
        "- Have you been diagnosed with diabetes?"
        "- Have you been diagnosed with depression?"
        "- Do you have a history of significant head injury?"
        "- Have you been diagnosed with hypertension (high blood pressure)?"
        "- What is your typical Systolic Blood Pressure (mmHg)? (90-180, e.g., 120)"
        "- What is your typical Diastolic Blood Pressure (mmHg)? (60-120, e.g., 80)"
        "- What is your Total Cholesterol level (mg/dL)? (150-300)"
        "- What is your LDL Cholesterol level (mg/dL)? (50-200)"
        "- What is your HDL Cholesterol level (mg/dL)? (20-100)"
        "- What are your Triglycerides levels (mg/dL)? (50-400)"
        "- What is your Mini-Mental State Examination (MMSE) score? (0-30, lower scores indicate impairment)"
        "- What is your Functional Assessment score? (0-10, lower scores indicate greater impairment)"
        "- Do you experience memory complaints?"
        "- Have you experienced behavioral problems recently?"
        "- What is your Activities of Daily Living (ADL) score? (0-10, lower scores indicate greater impairment)"
        "- Do you experience episodes of confusion?"
        "- Do you experience episodes of disorientation (e.g., to time or place)?"
        "- Have you noticed significant personality changes?"
        "- Do you have difficulty completing familiar tasks?"
        "- Do you experience significant forgetfulness beyond what is typical?"
    )
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config=types.GenerateContentConfig(
            thinking_config=types.ThinkingConfig(thinking_budget=0)
        ),
    )
    text = response.text
    if text and isinstance(text, str) and text.startswith('"') and text.endswith('"'):
        text = text[1:-1]
    return text if text else ""