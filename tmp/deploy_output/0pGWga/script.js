// 计算器状态管理
class Calculator {
    constructor() {
        this.previousOperand = '';
        this.currentOperand = '0';
        this.operation = undefined;
        this.shouldResetScreen = false;
        this.status = '就绪';
    }

    // 清除所有数据
    clear() {
        this.previousOperand = '';
        this.currentOperand = '0';
        this.operation = undefined;
        this.status = '已清除';
    }

    // 删除最后一个字符
    delete() {
        if (this.currentOperand.length === 1) {
            this.currentOperand = '0';
        } else {
            this.currentOperand = this.currentOperand.slice(0, -1);
        }
        this.status = '删除最后一位';
    }

    // 添加数字
    appendNumber(number) {
        // 如果当前显示的是0或者需要重置屏幕，则替换当前内容
        if (this.currentOperand === '0' || this.shouldResetScreen) {
            this.currentOperand = number;
            this.shouldResetScreen = false;
        } else {
            // 否则追加数字
            this.currentOperand += number;
        }
        this.status = '输入数字';
    }

    // 选择操作符
    chooseOperation(operation) {
        // 如果当前没有操作数，则直接返回
        if (this.currentOperand === '') return;
        
        // 如果已有上一个操作数，则先计算
        if (this.previousOperand !== '') {
            this.calculate();
        }
        
        this.operation = operation;
        this.previousOperand = this.currentOperand;
        this.currentOperand = '';
        this.status = `选择操作: ${operation}`;
    }

    // 添加小数点
    addDecimal() {
        // 如果已经包含小数点，则不再添加
        if (this.currentOperand.includes('.')) return;
        
        // 如果当前为空或需要重置，则添加"0."
        if (this.currentOperand === '' || this.shouldResetScreen) {
            this.currentOperand = '0.';
            this.shouldResetScreen = false;
        } else {
            this.currentOperand += '.';
        }
        this.status = '添加小数点';
    }

    // 计算百分比
    calculatePercentage() {
        if (this.currentOperand === '') return;
        
        const current = parseFloat(this.currentOperand);
        this.currentOperand = (current / 100).toString();
        this.status = '计算百分比';
    }

    // 执行计算
    calculate() {
        let computation;
        const prev = parseFloat(this.previousOperand);
        const current = parseFloat(this.currentOperand);
        
        // 如果操作数无效，则直接返回
        if (isNaN(prev) || isNaN(current)) return;
        
        try {
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
                        throw new Error('除以零错误');
                    }
                    computation = prev / current;
                    break;
                default:
                    return;
            }
            
            // 处理浮点数精度问题
            this.currentOperand = this.roundResult(computation).toString();
            this.operation = undefined;
            this.previousOperand = '';
            this.shouldResetScreen = true;
            this.status = '计算完成';
        } catch (error) {
            this.currentOperand = '错误';
            this.previousOperand = '';
            this.operation = undefined;
            this.status = error.message;
        }
    }

    // 四舍五入结果以避免浮点数精度问题
    roundResult(number) {
        // 限制小数位数为10位
        return Math.round(number * 10000000000) / 10000000000;
    }

    // 更新显示
    updateDisplay() {
        const currentOperandElement = document.getElementById('current-operand');
        const previousOperandElement = document.getElementById('previous-operand');
        const statusIndicator = document.getElementById('status-indicator');
        
        // 更新当前操作数显示
        currentOperandElement.textContent = this.currentOperand;
        
        // 更新上一个操作数显示（如果有操作符）
        if (this.operation != null) {
            previousOperandElement.textContent = 
                `${this.previousOperand} ${this.operation}`;
        } else {
            previousOperandElement.textContent = '';
        }
        
        // 更新状态指示器
        statusIndicator.textContent = this.status;
    }
}

// 初始化计算器
const calculator = new Calculator();

// DOM加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    // 获取所有按钮
    const numberButtons = document.querySelectorAll('[data-number]');
    const operationButtons = document.querySelectorAll('[data-action]');
    
    // 为数字按钮添加点击事件
    numberButtons.forEach(button => {
        button.addEventListener('click', () => {
            calculator.appendNumber(button.textContent);
            calculator.updateDisplay();
        });
    });
    
    // 为操作按钮添加点击事件
    operationButtons.forEach(button => {
        button.addEventListener('click', () => {
            const action = button.getAttribute('data-action');
            
            switch (action) {
                case 'clear':
                    calculator.clear();
                    break;
                case 'backspace':
                    calculator.delete();
                    break;
                case 'decimal':
                    calculator.addDecimal();
                    break;
                case 'percentage':
                    calculator.calculatePercentage();
                    break;
                case 'calculate':
                    calculator.calculate();
                    break;
                case 'add':
                case 'subtract':
                case 'multiply':
                case 'divide':
                    // 获取按钮显示的符号
                    const operationMap = {
                        'add': '+',
                        'subtract': '-',
                        'multiply': '×',
                        'divide': '÷'
                    };
                    calculator.chooseOperation(operationMap[action]);
                    break;
            }
            
            calculator.updateDisplay();
        });
    });
    
    // 初始更新显示
    calculator.updateDisplay();
    
    // 添加键盘支持
    document.addEventListener('keydown', event => {
        // 防止默认行为（如空格键滚动页面）
        if (event.key.match(/[0-9\.\+\-\*\/=]|Enter|Backspace|Delete|Escape/)) {
            event.preventDefault();
        }
        
        // 处理数字键
        if (event.key >= '0' && event.key <= '9') {
            calculator.appendNumber(event.key);
            calculator.updateDisplay();
        }
        
        // 处理小数点
        if (event.key === '.') {
            calculator.addDecimal();
            calculator.updateDisplay();
        }
        
        // 处理操作符
        if (event.key === '+') {
            calculator.chooseOperation('+');
            calculator.updateDisplay();
        }
        
        if (event.key === '-') {
            calculator.chooseOperation('-');
            calculator.updateDisplay();
        }
        
        if (event.key === '*') {
            calculator.chooseOperation('×');
            calculator.updateDisplay();
        }
        
        if (event.key === '/') {
            calculator.chooseOperation('÷');
            calculator.updateDisplay();
        }
        
        // 处理等号和回车键
        if (event.key === '=' || event.key === 'Enter') {
            calculator.calculate();
            calculator.updateDisplay();
        }
        
        // 处理退格键
        if (event.key === 'Backspace') {
            calculator.delete();
            calculator.updateDisplay();
        }
        
        // 处理删除和ESC键（清除）
        if (event.key === 'Delete' || event.key === 'Escape') {
            calculator.clear();
            calculator.updateDisplay();
        }
        
        // 处理百分比
        if (event.key === '%') {
            calculator.calculatePercentage();
            calculator.updateDisplay();
        }
    });
    
    // 添加按钮点击反馈效果
    const allButtons = document.querySelectorAll('.btn');
    allButtons.forEach(button => {
        button.addEventListener('mousedown', () => {
            button.style.opacity = '0.8';
        });
        
        button.addEventListener('mouseup', () => {
            button.style.opacity = '1';
        });
        
        button.addEventListener('mouseleave', () => {
            button.style.opacity = '1';
        });
    });
});