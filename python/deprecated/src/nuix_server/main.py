import os
from fastapi import FastAPI
import uvicorn 
from .router import page, daily, record
from .spec import ROOT_PATH

app = FastAPI()
app.include_router(page.router)
app.include_router(daily.router)
app.include_router(record.router)

def main():
    os.makedirs(ROOT_PATH, exist_ok=True)
    uvicorn.run("nuix_server.main:app", host='0.0.0.0', port=26665, reload=True, workers=4)

if __name__ == '__main__':
    main()