const pagination = require('hexo-pagination');

module.exports = function (hexo) {
    // ATTENTION: This will override the default category generator!
    hexo.extend.generator.register('category', function(locals) {
        const config = this.config;
        const perPage = config.category_generator.per_page;
        const paginationDir = config.pagination_dir || 'page';

        function findParent(category) {
            let parents = [];
            if (category && category.hasOwnProperty('parent')) {
                const parent = locals.categories.filter(cat => cat._id === category.parent).first();
                parents = [parent].concat(findParent(parent));
            }
            return parents;
        }
        
        return locals.categories.reduce(function(result, category){
            const posts = category.posts.sort('-date');
            const data = pagination(category.path, posts, {
                perPage: perPage,
                layout: ['category', 'archive', 'index'],
                format: paginationDir + '/%d/',
                data: {
                    category: category.name,
                    parents: findParent(category)
                }
            });
        
            return result.concat(data);
        }, []);
    });
}