import os
import zipfile
from typing import Annotated
from fastapi import APIRouter, UploadFile, Form, File
from ..spec import ROOT_PATH

router = APIRouter(
    prefix='/record',
    tags=['record'],
)

def unzip(file, dst):
    if zipfile.is_zipfile(file):
        fz = zipfile.ZipFile(file, 'r')
        for f in fz.namelist():
            fz.extract(f, dst)

@router.post("/")
def upload(
    path: Annotated[str, Form()],
    file: Annotated[UploadFile, File()] = None,
):
    print(path)
    folder = os.path.join(ROOT_PATH, '.' + path)
    os.makedirs(folder, exist_ok=True)
    if file:
        zip_path = os.path.join(folder, file.filename)
        with open(zip_path, 'wb') as fout:
            fout.write(file.file.read())
        unzip(file.file, ROOT_PATH)
    return {}
