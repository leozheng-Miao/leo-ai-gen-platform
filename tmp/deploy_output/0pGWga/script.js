// 计算器状态管理
class Calculator {
    constructor() {
        this.previousOperand = '';
        this.currentOperand = '0';
        this.operation = undefined;
        this.history = [];
        this.initializeEventListeners();
        this.updateDisplay();
    }
    
    // 清除所有内容
    clear() {
        this.previousOperand = '';
        this.currentOperand = '0';
        this.operation = undefined;
    }
    
    // 删除最后一个字符
    backspace() {
        if (this.currentOperand.length === 1) {
            this.currentOperand = '0';
        } else {
            this.currentOperand = this.currentOperand.slice(0, -1);
        }
    }
    
    // 添加数字
    appendNumber(number) {
        // 防止输入多个小数点
        if (number === '.' && this.currentOperand.includes('.')) return;
        
        // 如果当前是0，替换它（除非是0.）
        if (this.currentOperand === '0' && number !== '.') {
            this.currentOperand = number;
        } else {
            this.currentOperand += number;
        }
    }
    
    // 选择操作符
    chooseOperation(operation) {
        // 如果当前没有输入数字，不执行操作
        if (this.currentOperand === '') return;
        
        // 如果已经有之前的操作数和操作符，先计算
        if (this.previousOperand !== '') {
            this.compute();
        }
        
        this.operation = operation;
        this.previousOperand = this.currentOperand;
        this.currentOperand = '';
    }
    
    // 执行计算
    compute() {
        let computation;
        const prev = parseFloat(this.previousOperand);
        const current = parseFloat(this.currentOperand);
        
        // 如果操作数不是数字，不执行计算
        if (isNaN(prev) || isNaN(current)) return;
        
        switch (this.operation) {
            case '+':
                computation = prev + current;
                break;
            case '-':
                computation = prev - current;
                break;
            case '×':
                computation = prev * current;
                break;
            case '÷':
                if (current === 0) {
                    alert('错误：不能除以零！');
                    this.clear();
                    this.updateDisplay();
                    return;
                }
                computation = prev / current;
                break;
            case '%':
                computation = prev % current;
                break;
            default:
                return;
        }
        
        // 保存到历史记录
        this.addToHistory(`${this.previousOperand} ${this.operation} ${this.currentOperand} = ${computation}`);
        
        // 更新当前操作数为计算结果
        this.currentOperand = computation.toString();
        this.operation = undefined;
        this.previousOperand = '';
    }
    
    // 添加小数点
    addDecimal() {
        if (!this.currentOperand.includes('.')) {
            this.currentOperand += '.';
        }
    }
    
    // 计算百分比
    calculatePercentage() {
        if (this.currentOperand === '') return;
        
        const current = parseFloat(this.currentOperand);
        this.currentOperand = (current / 100).toString();
    }
    
    // 更新显示
    updateDisplay() {
        const currentOperandElement = document.getElementById('current-operand');
        const previousOperandElement = document.getElementById('previous-operand');
        
        currentOperandElement.textContent = this.currentOperand;
        
        if (this.operation != null) {
            previousOperandElement.textContent = `${this.previousOperand} ${this.operation}`;
        } else {
            previousOperandElement.textContent = this.previousOperand;
        }
    }
    
    // 添加到历史记录
    addToHistory(calculation) {
        this.history.unshift(calculation);
        
        // 只保留最近的10条记录
        if (this.history.length > 10) {
            this.history.pop();
        }
        
        this.updateHistoryDisplay();
    }
    
    // 更新历史记录显示
    updateHistoryDisplay() {
        const historyList = document.getElementById('history-list');
        historyList.innerHTML = '';
        
        this.history.forEach(item => {
            const li = document.createElement('li');
            li.textContent = item;
            historyList.appendChild(li);
        });
    }
    
    // 清除历史记录
    clearHistory() {
        this.history = [];
        this.updateHistoryDisplay();
    }
    
    // 初始化事件监听器
    initializeEventListeners() {
        // 数字按钮
        document.querySelectorAll('[data-number]').forEach(button => {
            button.addEventListener('click', () => {
                this.appendNumber(button.getAttribute('data-number'));
                this.updateDisplay();
                
                // 添加点击动画
                button.classList.add('clicked');
                setTimeout(() => button.classList.remove('clicked'), 150);
            });
        });
        
        // 操作符按钮
        document.querySelectorAll('[data-action]').forEach(button => {
            button.addEventListener('click', () => {
                const action = button.getAttribute('data-action');
                
                switch (action) {
                    case 'add':
                    case 'subtract':
                    case 'multiply':
                    case 'divide':
                    case 'percentage':
                        const operationMap = {
                            'add': '+',
                            'subtract': '-',
                            'multiply': '×',
                            'divide': '÷',
                            'percentage': '%'
                        };
                        this.chooseOperation(operationMap[action]);
                        break;
                    case 'equals':
                        this.compute();
                        break;
                    case 'clear':
                        this.clear();
                        break;
                    case 'backspace':
                        this.backspace();
                        break;
                    case 'decimal':
                        this.addDecimal();
                        break;
                }
                
                this.updateDisplay();
                
                // 添加点击动画
                button.classList.add('clicked');
                setTimeout(() => button.classList.remove('clicked'), 150);
            });
        });
        
        // 清除历史按钮
        document.getElementById('clear-history').addEventListener('click', () => {
            this.clearHistory();
        });
        
        // 键盘支持
        document.addEventListener('keydown', event => {
            // 数字键
            if (event.key >= '0' && event.key <= '9') {
                this.appendNumber(event.key);
                this.updateDisplay();
            }
            
            // 小数点
            else if (event.key === '.') {
                this.addDecimal();
                this.updateDisplay();
            }
            
            // 操作符
            else if (event.key === '+') {
                this.chooseOperation('+');
                this.updateDisplay();
            }
            else if (event.key === '-') {
                this.chooseOperation('-');
                this.updateDisplay();
            }
            else if (event.key === '*') {
                this.chooseOperation('×');
                this.updateDisplay();
            }
            else if (event.key === '/') {
                event.preventDefault(); // 防止浏览器搜索快捷键
                this.chooseOperation('÷');
                this.updateDisplay();
            }
            
            // 等号或回车
            else if (event.key === '=' || event.key === 'Enter') {
                this.compute();
                this.updateDisplay();
            }
            
            // 退格键
            else if (event.key === 'Backspace') {
                this.backspace();
                this.updateDisplay();
            }
            
            // 清除键 (Escape)
            else if (event.key === 'Escape') {
                this.clear();
                this.updateDisplay();
            }
        });
    }
}

// 页面加载完成后初始化计算器
document.addEventListener('DOMContentLoaded', () => {
    const calculator = new Calculator();
    
    // 添加一些初始历史记录作为示例
    calculator.addToHistory('12 + 8 = 20');
    calculator.addToHistory('15 × 3 = 45');
    calculator.addToHistory('100 ÷ 4 = 25');
    
    // 添加CSS动画类
    const style = document.createElement('style');
    style.textContent = `
        .clicked {
            transform: scale(0.95) !important;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
        }
    `;
    document.head.appendChild(style);
});