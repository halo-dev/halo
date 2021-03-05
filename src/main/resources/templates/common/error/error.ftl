<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <link rel="alternate" type="application/rss+xml" title="atom 1.0" href="${atom_url!}">
    <title>${(status)!500} | ${(error)!'未知错误'}</title>

    <style type="text/css">
        body {
            padding: 30px 20px;
            font-family: -apple-system, BlinkMacSystemFont,
            "Segoe UI", "Roboto", "Oxygen", "Ubuntu", "Cantarell",
            "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif;
            color: #727272;
            line-height: 1.6;
        }

        .container {
            max-width: 500px;
            margin: 0 auto;
        }

        h1 {
            margin: 0;
            font-size: 60px;
            line-height: 1;
            color: #252427;
            font-weight: 700;
            display: inline-block;
        }

        h2 {
            margin: 100px 0 0;
            font-weight: 600;
            letter-spacing: 0.1em;
            color: #A299AC;
            text-transform: uppercase;
        }

        p {
            font-size: 16px;
            margin: 1em 0;
        }

        @media screen and (min-width: 768px) {
            body {
                padding: 50px;
            }
        }

        @media screen and (max-width: 480px) {
            h1 {
                font-size: 48px;
            }
        }

        .title {
            position: relative;
        }

        .title::before {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 2px;
            background-color: #000;
            transform-origin: bottom right;
            transform: scaleX(0);
            transition: transform 0.5s ease;
        }

        .title:hover::before {
            transform-origin: bottom left;
            transform: scaleX(1);
        }

        .back-home button {
            z-index: 1;
            position: relative;
            font-size: inherit;
            font-family: inherit;
            color: white;
            padding: 0.5em 1em;
            outline: none;
            border: none;
            background-color: hsl(0, 0%, 0%);
            overflow: hidden;
            transition: color 0.4s ease-in-out;
        }

        .back-home button::before {
            content: '';
            z-index: -1;
            position: absolute;
            top: 100%;
            left: 100%;
            width: 1em;
            height: 1em;
            border-radius: 50%;
            background-color: #fff;
            transform-origin: center;
            transform: translate3d(-50%, -50%, 0) scale3d(0, 0, 0);
            transition: transform 0.45s ease-in-out;
        }

        .back-home button:hover {
            cursor: pointer;
            color: #000;
        }

        .back-home button:hover::before {
            transform: translate3d(-50%, -50%, 0) scale3d(15, 15, 15);
        }
    </style>
</head>

<body>

<div class="container">
    <h2>${(status)!500}</h2>
    <h1 class="title">${(error)!'未知错误'}.</h1>
    <p>${(message)!'未知错误！可能存在的原因：未正确设置主题或主题文件缺失。'}</p>
    <div class="back-home">
        <button onclick="window.location.href='${blog_url!}'">首页</button>
    </div>
</div>
</body>

</html>