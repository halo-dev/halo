function Queue() {
    this.dataStore = [];
    this.offer = offer;
    this.poll = poll;
    this.execNext = execNext;
    this.debug = false;
    this.startDebug = startDebug;


    function offer(element) {
        if(this.debug){
            console.log('Offered a Queued Function.');
        }
        if(typeof element === 'function') {
            this.dataStore.push(element);
        } else {
            console.log('You must offer a function.');
        }
    }

    function poll() {
        if(this.debug){
            console.log('Polled a Queued Function.');
        }
        return this.dataStore.shift();
    }

    function execNext() {
        var nextfunc = this.poll();
        if(nextfunc !== undefined) {
            if(this.debug){
                console.log('Run a Queued Function.');
            }
            nextfunc();
        }
    }

    function startDebug(){
        this.debug = true;
    }

}

var queue = new Queue();