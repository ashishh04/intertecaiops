FROM python:3.11-slim

WORKDIR /app

# Copy requirements and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY src/ ./src/

# Expose port
EXPOSE 8000

# Set environment path to load properly
ENV PYTHONPATH=/app/src

# Command to run the application (production level)
# We override reload=True in docker by calling uvicorn directly
CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000"]
