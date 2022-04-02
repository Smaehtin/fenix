#!/usr/bin/python

import os
import requests
import sys

def main():
    home = os.getenv('HOME')

    print('Getting latest release asset')

    response = requests.get(
        'https://api.github.com/repos/Smaehtin/fenix/releases',
        headers={
            'Accept': 'application/vnd.github.v3+json'
        },
    )
    data = response.json()

    asset = data[0]['assets'][0]
    asset_id = str(asset['id'])
    asset_name = asset['name']

    print(f'Found release asset, id={asset_id}, name={asset_name}')

    last_asset_id_path = f'{home}/.last_fenix_download_id'
    last_asset_id = None
    try:
        last_asset_id = open(last_asset_id_path, 'r').read()
    except:
        pass

    if asset_id == last_asset_id:
        print('Asset ID matches last downloaded asset, stopping')
        sys.exit(1)

    download_url = asset['browser_download_url']

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
    open(last_asset_id_path, 'w').write(asset_id)

main()
