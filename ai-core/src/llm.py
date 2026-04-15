import os
from langchain_openai import AzureChatOpenAI
from dotenv import load_dotenv

# Load root .env
load_dotenv(dotenv_path="../../.env")

def get_llm():
    """
    Returns an instance of AzureChatOpenAI configured with environment variables.
    """
    return AzureChatOpenAI(
        azure_deployment=os.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT_NAME", "gpt-4o"),
        openai_api_version=os.getenv("AZURE_OPENAI_API_VERSION", "2025-01-01-preview"),
        azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT", ""),
        api_key=os.getenv("AZURE_OPENAI_API_KEY", ""),
        temperature=0.0
    )
