import os
import cv2 as cv
import datetime

## returns jpgs from top_matches folder and user can select one, than returns information about selected coin
# from coins_info folder

selected_image = None


def on_mouse_click(event, x, y, flags, param):
    global selected_image
    if event == cv.EVENT_LBUTTONDOWN:
        print(f"Image '{param}' selected.")
        selected_image = param
        cv.destroyAllWindows()


def select(folder: str):
    global selected_image

    if not os.path.exists(folder):
        print(f"Error: Folder '{folder}' does not exist.")
        return None

    images = [f for f in os.listdir(folder) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

    if not images:
        print("No valid images found in the folder.")
        return None

    for idx, filename in enumerate(images):
        file_path = os.path.join(folder, filename)
        img = cv.imread(file_path)

        if img is None:
            print(f"Skipping '{filename}' (not a valid image)")
            continue

        window_name = f"Coin {idx + 1}: {filename}"
        cv.imshow(window_name, img)
        cv.setMouseCallback(window_name, on_mouse_click, filename)

    cv.waitKey(0)
    cv.destroyAllWindows()

    if selected_image:
        print(f"You selected: {selected_image}")
    else:
        print("No image selected.")

    return selected_image


def get_text(year, name):
    wanted_folder = f"coins_info/{year}"
    if not os.path.exists(wanted_folder):
        print(f"Error: Folder '{wanted_folder}' does not exist.")
        return None

    texts = [f for f in os.listdir(wanted_folder) if f.endswith('.txt')]
    matched_text_file = None
    matched_words_count = 0

    for text_file in texts:
        text_name = text_file.replace(".txt", "")
        if name and name == text_name:
            matched_text_file = text_file
            break
        if text_name.__contains__(name):
            matched_words_count += 1
        if matched_words_count == 1:
            matched_text_file = text_file
            break

    if not matched_text_file:
        print(f"Error: No matching text file found for '{name}' in {wanted_folder}")
        return None

    text_path = os.path.join(wanted_folder, matched_text_file)

    with open(text_path, "r") as f:
        return f.read()


folder = "coins_images/top_matches"

# Extract available text files to determine the latest year
txt_files = [f for f in os.listdir(folder) if f.endswith('.txt')]
input_year = None

if txt_files:
    try:
        input_year = int(txt_files[0].replace(".txt", ""))
    except ValueError:
        print(f"Warning: '{txt_files[0]}' does not contain a valid year.")
        input_year = None
else:
    print("No .txt file found in the folder.")

# Validate the year
if input_year and datetime.datetime.now().year <= input_year:
    print(f"You have selected '{input_year}' year. Information about coins from this year is unavailable on the "
          f"https://www.ecb.europa.eu/euro/coins/comm/html/index.en.html.")
    input_year = None  # Reset to prevent errors

# Display all coins at once & allow selection
selected_img = select(folder)

if selected_img:
    selected_img = selected_img.replace(".jpg", "").replace(".png", "").replace(".jpeg", "")

# Fetch text data if year is valid
selected_text = None
if input_year and selected_img:
    selected_text = get_text(input_year, selected_img)

if selected_text:
    print(selected_text)
else:
    print("No coin information found.")
