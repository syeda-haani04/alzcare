import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load your data
df = pd.read_csv('alzheimers_disease_data.csv')

# Drop non-numeric columns for correlation
numeric_df = df.drop(columns=['PatientID', 'DoctorInCharge'], errors='ignore')

# Correlation heatmap
plt.figure(figsize=(16, 12))
corr = numeric_df.corr()
sns.heatmap(corr, annot=True, cmap='coolwarm', vmin=-0.1, vmax=0.1)
plt.title('Feature Correlation Heatmap')
plt.tight_layout()
plt.savefig('correlation_heatmap.png')
plt.close()

# Distribution of Age
plt.figure(figsize=(8, 6))
sns.histplot(data=df, x='Age', bins=20, kde=True)  # <-- fixed usage
plt.title('Age Distribution')
plt.xlabel('Age')
plt.ylabel('Count')
plt.tight_layout()
plt.savefig('age_distribution.png')
plt.close()

# Boxplot of BMI by Diagnosis
plt.figure(figsize=(8, 6))
sns.boxplot(x='Diagnosis', y='BMI', data=df)
plt.title('BMI by Diagnosis')
plt.xlabel('Diagnosis')
plt.ylabel('BMI')
plt.tight_layout()
plt.savefig('bmi_by_diagnosis.png')
plt.close()

# Pairplot of selected features
selected_features = ['Age', 'BMI', 'MMSE', 'Diagnosis']
sns.pairplot(df[selected_features], hue='Diagnosis')
plt.savefig('pairplot_selected_features.png')
plt.close()