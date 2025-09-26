import json
import requests
import sseclient

class TestNuix():
  def test_nuix(self):
    response = requests.post('http://localhost:26665/nuix/user/session', data={'sessionName': 'Session'})
    assert response.status_code == 200
    sessionId = json.loads(response.content)['sessionId']

    response = requests.post(
      url='http://localhost:26665/nuix/user/session/query',
      data={'sessionId': sessionId, 'stream': True, 'instruction': 'The quick brown fox jumps over the lazy dog'},
      stream=True,
    )
    print(response.content)

    # response = requests.post(
    #   url='http://localhost:26665/nuix/user/session/query',
    #   data={'sessionId': sessionId, 'stream': False, 'instruction': 'The quick brown fox jumps over the lazy dog'},
    #   stream=True,
    # )
    # for chunk in response:
    #   print(chunk.decode('utf-8'), end = '')
    # print()

    # response = requests.post(
    #   url='http://localhost:26665/nuix/user/session/query',
    #   data={'sessionId': sessionId, 'stream': False, 'instruction': '今天北京天气怎么样？'},
    #   stream=True,
    # )
    # for chunk in response:
    #   print(chunk.decode('utf-8'), end = '')
    # print()

    # response = requests.post(
    #   url='http://localhost:26665/nuix/user/session/query',
    #   data={'sessionId': sessionId, 'stream': False, 'instruction': '写一首英文诗'},
    #   stream=True,
    # )
    # for chunk in response:
    #   print(chunk.decode('utf-8'), end = '')
