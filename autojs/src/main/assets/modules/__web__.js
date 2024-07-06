

module.exports = function(__runtime__, scope){
    scope.newInjectableWebClient = function(){
        return new com.stardust.atjs.core.web.InjectableWebClient(org.mozilla.javascript.Context.getCurrentContext(), scope);
    }

    scope.newInjectableWebView = function(activity){
        return new com.stardust.atjs.core.web.InjectableWebView(scope.activity, org.mozilla.javascript.Context.getCurrentContext(), scope);
    }
}


