import os
import requests
from bs4 import BeautifulSoup
from datetime import datetime

def download_info(base_path):
    base_url_info = "https://www.ecb.europa.eu/euro/coins/comm/html/comm_{}.en.html"
    info_folder = os.path.join(base_path, "coins_info")
    if not os.path.exists(info_folder):
        os.makedirs(info_folder)

    curr_year = datetime.now().year
    while curr_year >= 2004:
        url2 = base_url_info.format(curr_year)
        response = requests.get(url2)

        if response.status_code != 200:
            print(f"Could not access {url2}")
            curr_year -= 1
            continue

        soup = BeautifulSoup(response.text, "html.parser")
        coin_sections = soup.find_all()
        year_folder_info = os.path.join(info_folder, str(curr_year))
        if not os.path.exists(year_folder_info):
            os.makedirs(year_folder_info)

        for section in coin_sections:
            coin_info_text = section.get_text(separator="\n", strip=True)
            feature_start = coin_info_text.find("Feature:")
            description_start = coin_info_text.find("Description:")

            if feature_start == -1 or description_start == -1:
                continue

            feature_text = coin_info_text[feature_start + len("Feature:"):description_start].strip()
            coin_name_cleaned = (feature_text.replace("__________", "_").replace("/", "_")
                                 .replace("\n          ", "_").replace(" ", "_").lower())

            txt_filename = os.path.join(year_folder_info, f"{coin_name_cleaned}.txt")

            with open(txt_filename, "w", encoding="utf-8") as f:
                f.write(f"Coin Name: {feature_text}\n")
                f.write(f"Year: {curr_year}\n")
                f.write(f"Details:\n{coin_info_text}\n")

            print(f"Successfully downloaded: {txt_filename}")
        curr_year -= 1

    print('All texts have been successfully downloaded in', info_folder)