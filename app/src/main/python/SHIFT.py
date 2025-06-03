import numpy as np
import cv2 as cv
import os
import json
import sys

def log(msg):
    print(msg)
    sys.stdout.flush()


def resize_image(img, max_dim=800):
    h, w = img.shape[:2]
    if max(h, w) > max_dim:
        scale = max_dim / max(h, w)
        return cv.resize(img, (int(w * scale), int(h * scale)))
    return img


def sift_similarity(img1_path, img2_path):
    i1 = resize_image(cv.imread(img1_path))
    i2 = resize_image(cv.imread(img2_path))

    if i1 is None or i2 is None:
        print(f"Error loading images: {img1_path} or {img2_path}")
        return -1

    img1 = cv.cvtColor(i1, cv.COLOR_BGR2GRAY)
    img2 = cv.cvtColor(i2, cv.COLOR_BGR2GRAY)

    sift = cv.SIFT_create()
    k_1, des_1 = sift.detectAndCompute(img1, None)
    k_2, des_2 = sift.detectAndCompute(img2, None)

    if des_1 is None or des_2 is None:
        print(f"Skipping {img1_path} due to no keypoints.")
        return -1

    bf = cv.BFMatcher(cv.NORM_L2 , crossCheck=True)
    matches = bf.match(des_1, des_2)

    if not matches:
        return -1

    matches = sorted(matches, key=lambda x: x.distance)
    return sum(m.distance for m in matches[:50]) / min(len(matches), 50)


def find_top_similar_images(base_folder, target_image_path, year, top_n=0):
    try:
        if year == "circulation_coins":
            folder_path = os.path.join(base_folder, "circulation_coins")
        else:
            folder_path = os.path.join(base_folder, f"coins_images/{year}")

        all_similarities = []
        print(f"starting in: {base_folder}")

        if not os.path.exists(folder_path):
            print(f"Skipping: {folder_path} (Folder not found)")
            return json.dumps([])

        image_files = [f for f in os.listdir(folder_path) if f.endswith('.jpg')]

        for image_file in image_files:
            image_path = os.path.join(folder_path, image_file)
            score = sift_similarity(image_path, target_image_path)
            print(f"image: {image_file} has score: {score}")
            if score != -1:
                all_similarities.append((image_path, score))

        all_similarities.sort(key=lambda x: x[1])
        if top_n == 0:
            top_matches = all_similarities
        else:
            top_matches = all_similarities[:top_n]
        print("ALL DONE!!")

        result = [{"path": path, "score": score} for path, score in top_matches]
        return json.dumps(result)

    except Exception as e:
        print(f"ERROR: {e}")
        return json.dumps([])


def compare_single_image(screenshot_path, image_path):
    return sift_similarity(screenshot_path, image_path)
