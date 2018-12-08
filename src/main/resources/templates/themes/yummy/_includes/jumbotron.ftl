<#macro jumbotron title,date>
<section class="jumbotron geopattern" data-pattern-id="${title}">
    <div class="container">
        <div id="jumbotron-meta-info">
            <h1>${title}</h1>
            <span class="meta-info">
                <span class="octicon octicon-calendar"></span> ${date?if_exists}
            </span>
        </div>
    </div>
</section>
<script>
    $(document).ready(function(){

        $('.geopattern').each(function(){
            $(this).geopattern($(this).data('pattern-id'));
        });

    });
</script>
</#macro>
