### Lesson Learned

#### Lombok Issues

##### Annotation Processor Error

When I run the test, Intellij complain about:
"java: variable postService not initialized in the default constructor"

How to solve  
1. Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Switch to “obtain processors from project classpath”

