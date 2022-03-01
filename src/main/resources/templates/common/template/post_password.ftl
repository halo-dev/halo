<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
    <meta name="robots" content="noindex,nofllow"/>
    <title>私密文章访问 - ${blog_title!}</title>
    <style>
        body {
            background-color: #080821;
        }

        .container {
            position: absolute;
            top: 50%;
            left: 50%;
            margin: -140px 0 0 -140px;
            width: 280px;
        }

        .password-input {
            position: relative;
        }

        .password-input input {
            box-sizing: border-box;
            width: 100%;
            color: white;
            font-size: inherit;
            font-family: inherit;
            background-color: hsl(236, 32%, 26%);
            padding: 0.5em 1em;
            border: 1px solid transparent;
            transition: background-color 0.3s ease-in-out;
        }

        .password-input input:focus {
            outline: none;
        }

        .password-input input::placeholder {
            color: hsla(0, 0%, 100%, 0.6);
        }

        .password-input span {
            position: absolute;
            background-color: #fc2f70;
            transition: transform 0.1s ease;
        }

        .password-input .bottom,
        .password-input .top {
            height: 1px;
            left: 0;
            right: 0;
            transform: scaleX(0);
        }

        .password-input .left,
        .password-input .right {
            width: 1px;
            top: 0;
            bottom: 0;
            transform: scaleY(0);
        }

        .password-input .bottom {
            bottom: 0;
            transform-origin: bottom right;
        }

        .password-input input:focus ~ .bottom {
            transform-origin: bottom left;
            transform: scaleX(1);
        }

        .password-input .right {
            right: 0;
            transform-origin: top right;
            transition-delay: 0.05s;
        }

        .password-input input:focus ~ .right {
            transform-origin: bottom right;
            transform: scaleY(1);
        }

        .password-input .top {
            top: 0;
            transform-origin: top left;
            transition-delay: 0.15s;
        }

        .password-input input:focus ~ .top {
            transform-origin: top right;
            transform: scaleX(1);
        }

        .password-input .left {
            left: 0;
            transform-origin: bottom left;
            transition-delay: 0.25s;
        }

        .password-input input:focus ~ .left {
            transform-origin: top left;
            transform: scaleY(1);
        }

        .submit-input {
            margin-top: 20px;
        }

        .submit-input button {
            width: 100%;
            z-index: 1;
            position: relative;
            font-size: inherit;
            font-family: inherit;
            color: white;
            padding: 0.5em 1em;
            outline: none;
            border: 1px solid transparent;
            background-color: hsl(236, 32%, 26%);
        }

        .submit-input button:hover {
            cursor: pointer;
        }

        .submit-input button::before {
            content: '';
            z-index: -1;
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            border: 4px solid hsl(236, 32%, 26%);
            transform-origin: center;
            transform: scale(1);
        }

        .submit-input button:hover::before {
            transition: all 0.75s ease-in-out;
            transform-origin: center;
            transform: scale(1.75);
            opacity: 0;
        }
    </style>
</head>
<body>
<div class="container">
    <form method="post" action="${blog_url!}/content/${type!}/${slug!}/authentication">
        <div class="password-input">
            <input type="password" name="password" placeholder="请输入访问密码">
            <span class="bottom"></span>
            <span class="right"></span>
            <span class="top"></span>
            <span class="left"></span>
        </div>
        <div style="margin-top: 8px;color: red;">${errorMsg!}</div>
        <div class="submit-input">
            <button type="submit">验证</button>
        </div>
    </form>
</div>
</body>
</html>