-- Create ratings table for user feedback and ratings
CREATE TABLE IF NOT EXISTS ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT NOT NULL,
    flight_number VARCHAR(20),
    rating_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_rating (username)
);

-- Insert some sample ratings for testing
INSERT INTO ratings (username, rating, feedback, flight_number) VALUES
('john_doe', 5, 'Excellent service! The flight was on time and the staff was very friendly.', 'A101'),
('jane_smith', 4, 'Great experience overall. Clean aircraft and smooth flight.', 'A115'),
('mike_wilson', 5, 'Outstanding service from check-in to landing. Highly recommended!', 'A101'),
('sarah_jones', 3, 'Flight was okay, but there was a slight delay. Staff was helpful though.', 'A115'),
('david_brown', 4, 'Good experience. The seats were comfortable and the food was decent.', 'A101'); 