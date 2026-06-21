from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import plan_generation, fitness_qa, feedback

app = FastAPI(title="Gym AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(plan_generation.router, prefix="/api/ai", tags=["plan"])
app.include_router(fitness_qa.router, prefix="/api/ai", tags=["qa"])
app.include_router(feedback.router, prefix="/api/ai", tags=["feedback"])


@app.get("/health")
async def health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
