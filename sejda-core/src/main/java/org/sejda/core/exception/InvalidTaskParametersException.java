/*
 * Created on 24/giu/2010
 *
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.sejda.core.exception;

/**
 * Exception thrown after validation of the task parameters if the validation fails
 * 
 * @author Andrea Vacondio
 * 
 */
public class InvalidTaskParametersException extends TaskException {

    private static final long serialVersionUID = 1046358680829746043L;

    public InvalidTaskParametersException() {
        super();
    }

    public InvalidTaskParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTaskParametersException(String message) {
        super(message);
    }

    public InvalidTaskParametersException(Throwable cause) {
        super(cause);
    }
}