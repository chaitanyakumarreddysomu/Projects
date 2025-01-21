const voiceButton = document.getElementById('voice-button');
const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
const synthesis = window.speechSynthesis;
const notificationSound = document.getElementById('notification-sound');

recognition.lang = 'en-US';

recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript.toLowerCase();
    handleVoiceCommand(transcript);
};

voiceButton.addEventListener('click', () => {
    // Play notification sound and start recognition
    notificationSound.play();
    recognition.start();
});

// Function to handle keyboard shortcuts
function handleKeyboardShortcut(event) {
    // Check if Alt + Shift + A is pressed
    if (event.altKey && event.shiftKey && event.key === 'A') {
        // Prevent default action for this shortcut
        event.preventDefault();
        // Trigger the same functionality as the voice button click
        notificationSound.play();
        recognition.start();
    }
}

// Add event listener for keyboard shortcuts
document.addEventListener('keydown', handleKeyboardShortcut);

function handleVoiceCommand(command) {
    console.log(`Voice Command: ${command}`);
    let response = '';

    if (command.includes('search')) {
        const query = command.replace('search', '').trim();
        const encodedQuery = encodeURIComponent(query).replace(/%20/g, '+');

        fetch(`/api/search?ch=${encodedQuery}`)
            .then(response => response.text())
            .then(html => {
                if (html.includes('Sorry, no results found')) {
                    response = `Sorry, no results found for ${query}.`;
                    speak(response);
                } else {
                    window.location.href = `/products?ch=${encodedQuery}`;
                    response = `Searching for ${query}.`;
                    speak(response);
                }
            })
            .catch(error => {
                response = `An error occurred while searching for ${query}.`;
                speak(response);
                console.error('Error:', error);
            });

    } else if (command.includes('category')) {
        const query = command.replace('category', '').trim();
        const encodedQuery = encodeURIComponent(query).replace(/%20/g, '+');
        window.location.href = `/products?category=${encodedQuery}`;
        response = `Showing results for category ${query}.`;
        speak(response);

    } else if (command.includes('checkout') || command.includes('check out')) {
        window.location.href = '/user/orders';
        response = 'Proceeding to checkout.';
        speak(response);

    } else if (command.includes('login')) {
        window.location.href = '/signin';
        response = 'Redirecting to login page.';
        speak(response);

    } else if (command.includes('register')) {
        window.location.href = '/register';
        response = 'Redirecting to register page.';
        speak(response);

    } else if (command.includes('forgot password')) {
        window.location.href = '/forgot-password';
        response = 'Redirecting to forgot password page.';
        speak(response);

    } else if (command.includes('profile')) {
        window.location.href = '/user/profile';
        response = 'Redirecting to profile page.';
        speak(response);

    } else if (command.includes('logout')) {
        window.location.href = '/logout';
        response = 'Successfully logged out.';
        speak(response);

    } else if (command.includes('home')) {
        window.location.href = '/';
        response = 'Redirecting to home page.';
        speak(response);

    } else if (command.includes('shop')) {
        window.location.href = '/products';
        response = 'Redirecting to shop page.';
        speak(response);

    } else if (command.includes('cart')) {
        window.location.href = '/user/cart';
        response = 'Redirecting to cart page.';
        speak(response);

    } else if (command.includes('contact')) {
        window.location.href = '/contact';
        response = 'Redirecting to contact page.';
        speak(response);

    } else {
        response = 'I did not understand that command.';
        speak(response);
    }
}

function speak(text) {
    const utterance = new SpeechSynthesisUtterance(text);
    synthesis.speak(utterance);
}

function extractProductIdFromUrl(url) {
    const match = url.match(/\/product\/(\d+)/);
    return match ? match[1] : null;
}

function getUserId() {
    // Implementation for getting the user ID if needed
}
