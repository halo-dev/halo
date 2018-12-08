<h3 class="sidebar-title">My Popular Repositories</h3>
<div id="github-repo">
<div class="card text-center" v-for="repo in repos">
    <div class="thumbnail">
        <div class="card-image geopattern" data-pattern-id="{{ repo.name }}">
            <div class="card-image-cell">
                <h3 class="card-title">
                    <a href="{{ repo.html_url }}" target="_blank">{{ repo.name }}</a>
                </h3>
            </div>
        </div>
        <div class="caption">
            <div class="card-description">
                <p class="card-text">{{ repo.description }}</p>
            </div>
            <div class="card-text">
                <span data-toggle="tooltip" class="meta-info" title="{{ repo.stargazers_count }} stars">
                    <span class="octicon octicon-star"></span> {{ repo.stargazers_count }}
                </span>
                <span data-toggle="tooltip" class="meta-info" title="{{ repo.forks_count }} forks">
                    <span class="octicon octicon-git-branch"></span> {{ repo.forks_count }}
                </span>
                <span data-toggle="tooltip" class="meta-info" title="Last updated：{{ repo.updated_at }}">
                    <span class="octicon octicon-clock"></span>
                    <time datetime="{{ repo.updated_at }}" title="{{ repo.updated_at }}">{{ repo.updated_at}}</time>
                </span>
            </div>
        </div>
    </div>
</div>
</div>
<script src="//cdnjs.loli.net/ajax/libs/vue/2.5.9/vue.min.js"></script>
<script src="//unpkg.com/axios/dist/axios.min.js"></script>
<script>

    $(document).ready(function(){

        new Vue({
            el: '#github-repo',
            data:{
                repos: []
            },
            mounted(){
                axios.get("https://api.github.com/users/ruibaby/repos").then((response) => {
                    this.repos = response.data;
                }).catch(error => {
                        console.log(error);
                });
            }
        });

        // Enable bootstrap tooltip
        $("body").tooltip({ selector: '[data-toggle=tooltip]' });

        $('.geopattern').each(function(){
            $(this).geopattern($(this).data('pattern-id'));
        });

    });
</script>
