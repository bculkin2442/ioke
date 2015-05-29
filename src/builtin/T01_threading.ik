; Threading utilities - mostly just primitive stuff at the moment

Threading = Origin mimic do(

        documentation = "Threading holds the basics of concurrency"
        
        thread = method("Spin a thread up to execute a block", block,
                java:lang:Thread new(block)
        )
        
        thread! = method("Spin up & start a thread to execute a block", block,
                thread(block) run
        )

        current = method("Give the current thread",
                java:lang:Thread field:currentThread
        )
        
        internal:future = Origin mimic do(
                documentation = "Internal prototype of all futures"
                
                initialize = method(task,
                        @task = task
                )
                
                cancel! = method("Attempt to cancel a task, possibly interrupting it", interrupt?,
                        task cancel(interrupt?)
                )
                
                get = method("Get the result of this task, possibly waiting up to a set duration", duration nil,
                        unless(duration,
                                task get,
                                task get ; TODO actually do something different once we have units of time
                        )
                )
                
                cancelled? = method("Check if this task has been cancelled",
                        task isCancelled
                )
                
                done? = method("Check if this task is done",
                        task isDone
                )
        )
                
        internal:tpool = Origin mimic do(
                documentation = "Internal prototype of all thread pools"
                
                initialize = method(pool,
                        @pool = pool
                        @pool documentation = "Internal thread pool to execute with"
                )
                
                submit = method("Submit a block to the pool, returning a promise for whatever the block yields", block,
                        pool submit(block)
                )
                
                submitYielding = method("Submit a block to the pool, returning a promise for a specific object", block, obj,
                        pool submit(block, obj)
                )
                
                doAll
                
        )       
         
        pool = method("Create a new thread pool to execute pieces of code with",

        )
)
