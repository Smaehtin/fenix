#!/usr/bin/python
 
import re
import os
import requests
import sys
 
def get_file_name(response):
    return re.findall(
        'filename=(.+)',
        response.headers['content-disposition']
    )[0]
 
def main():
    home = os.getenv('HOME')
 
    response = requests.get(
        'https://api.github.com/repos/Smaehtin/fenix/releases',
        headers={
            'Accept': 'application/vnd.github.v3+json'
        },
    )
    data = response.json()

    assets = data[0]['assets']
    download_url = assets[0]['browser_download_url']
 
    file_response = requests.get(download_url)
    if not file_response.ok:
        print(
            f'Unable to download {download_url}, status code: {file_response.status_code}',
            file=sys.stderr
        )
        return
 
    file_name = get_file_name(file_response)
    destination = f'{home}/storage/downloads/{file_name}'
 
    open(destination, 'wb').write(file_response.content)
 
    os.system(f'termux-share {destination}')
 
main()