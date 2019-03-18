<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <script src="/static/halo-admin/plugins/sweetalert/sweetalert.min.js"></script>
</head>
<body>
<button onclick="choise()">选择框</button>
</body>
<script>
    function choise() {
        swal({
            title: "Are you sure?",
            text: "Once deleted, you will not be able to recover this imaginary file!",
            icon: "warning",
            buttons: ["取消", "确定"],
            dangerMode: true
        }).then(function (action) {
            if (action) {
                swal("Poof! Your imaginary file has been deleted!", {
                    icon: "success",
                    button: "确定"
                });
            } else {
                swal("Your imaginary file is safe!");
            }
        })
    }
</script>
</html>
