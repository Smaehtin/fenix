#!/usr/bin/python

import os
import requests
import sys

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

    destination = f'{home}/storage/downloads/fenix/beta.apk'

    os.makedirs(
        os.path.dirname(destination),
        exist_ok=True
    )
    open(destination, 'wb').write(file_response.content)

    os.system(f'termux-share {destination}')

main()