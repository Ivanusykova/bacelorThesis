import cv2 as cv
import numpy as np

def cropped_image(image_path):
    image = cv.imread(image_path)
    height, width = image.shape[:2]
    x, y, r = int(width/2), int(height/2), 300

    mask = np.zeros_like(image)
    cv.circle(mask, (x, y), r, (255, 255, 255), -1)

    masked_img = cv.bitwise_and(image, mask)
    cropped = masked_img[y - r:y + r, x - r:x + r]

    output_path = image_path.replace(".jpg", "_cropped.jpg")
    cv.imwrite(output_path, cropped)
    return output_path
