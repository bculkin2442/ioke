; Threading utilities - mostly just primitive stuff at the moment

Threading = Origin mimic do(

        thread = method("Spin a thread up to execute a block", block,
                java:lang:Thread new(block)
        )
        
        thread! = method("Spin up & start a thread to execute a block", block,
                thread(block) run
        )
)
