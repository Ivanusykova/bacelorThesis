import os
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin

def download_images(base_path):
    url = 'https://www.coin-database.com/series/eurozone-commemorative-2-euro-coins-2-euro.html'
    main_folder = os.path.join(base_path, "coins_images")

    if not os.path.exists(main_folder):
        os.makedirs(main_folder)

    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')
    img_tags = soup.find_all('img')

    for img in img_tags:
        img_url = img.get('src')
        full_url = urljoin(url, img_url)
        alt_text = img.get('alt', '').replace('2 euro coin ', '').strip()
        alt_parts = alt_text.split()
        year = alt_parts[-1] if alt_parts[-1].isdigit() and len(alt_parts[-1]) == 4 else "Unknown"
        alt_text_cleaned = ' '.join(alt_parts[:-3]) if year != "Unknown" else ' '.join(alt_parts)
        alt_text_cleaned = alt_text_cleaned.replace(' ', '_').replace('/', '_').lower().strip('|')

        year_folder = os.path.join(main_folder, year)
        os.makedirs(year_folder, exist_ok=True)

        filename = os.path.join(year_folder, f'{alt_text_cleaned}.jpg')
        img_response = requests.get(full_url)
        with open(filename, 'wb') as f:
            f.write(img_response.content)
        print(f'Successfully downloaded: {filename}')

    print('All information have been successfully downloaded in', main_folder)
