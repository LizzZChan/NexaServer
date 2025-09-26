import os
from typing import Annotated
from fastapi import APIRouter, UploadFile, Form, File
from ..agent import search
from ..spec import ROOT_PATH

router = APIRouter(
    prefix='/page',
    tags=['page'],
)

@router.post("/")
def intention_to_page(
    gesture: Annotated[str, Form()],
    ui_tree: Annotated[str, Form()],
    file: Annotated[UploadFile, File()] = None,
):
    with open(os.path.join(ROOT_PATH, file.filename), 'wb') as fout:
        fout.write(file.file.read())
    gesture = list(map(float, gesture.strip().split(',')))
    return { "result": search(ui_tree, gesture) }