let display = document.getElementById('result');

// Append character to display
function appendChar(char) {
    display.value += char;
}

// Clear the display
function clearDisplay() {
    display.value = '';
}

// Delete the last character
function deleteChar() {
    display.value = display.value.slice(0, -1);
}

// Calculate the result
function calculate() {
    try {
        // Replace % with /100 for percentage calculation
        let expression = display.value.replace('%', '/100');
        // Evaluate the expression safely
        display.value = eval(expression) || '';
    } catch (error) {
        display.value = 'Error';
    }
}