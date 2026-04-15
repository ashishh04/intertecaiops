from fastapi import FastAPI
from pydantic import BaseModel
import os
from dotenv import load_dotenv

# Load `.env` from root
load_dotenv(dotenv_path="../../.env")

app = FastAPI(title="AI Model Execution Core")

class HealthCheckResponse(BaseModel):
    status: str
    service: str

@app.get("/health", response_model=HealthCheckResponse)
async def health_check():
    return HealthCheckResponse(status="ok", service="ai-core")

@app.get("/config-check")
async def check_config():
    # Only returning true/false for security, never leak keys
    return {
        "azure_openai_configured": bool(os.getenv("AZURE_OPENAI_API_KEY")),
        "azure_endpoint": os.getenv("AZURE_OPENAI_ENDPOINT")
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
