from typing import Annotated
from fastapi import APIRouter

router = APIRouter(
    prefix='/daily',
    tags=['daily'],
)

@router.post("/text")
def update_daily_text():
    ...