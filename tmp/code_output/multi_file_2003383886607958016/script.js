document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const message = document.getElementById('message');
    
    // 简单的验证逻辑
    if (username && password) {
        message.textContent = '登录成功！';
        message.style.color = 'green';
        // 在实际应用中，这里会发送登录请求到服务器
        console.log('登录信息:', { username, password });
    } else {
        message.textContent = '请填写所有字段';
        message.style.color = 'red';
    }
});