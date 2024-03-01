package com.fern.chorradas.microservice;

/**
 * Represents the response that travels back to the requester upon having sent
 * an http request. The response is JSON serialised before it is sent back to
 * the browser.
 *
 * @param <T>
 * @author Miguel Arregui (miguel.arregui@gmail.com)
 */
public class ResponseMessage<T> {
    public enum Status {
        Success, Failure;
    }

    /**
     * When everything went ok
     *
     * @param obj
     * @return status == Success
     */
    public static <T> ResponseMessage<T> success(T obj) {
        return new ResponseMessage<>(Status.Success, obj);
    }

    /**
     * When something went coo-coo
     *
     * @param obj
     * @return status == Failure
     */
    public static <T> ResponseMessage<T> failure(T obj) {
        return new ResponseMessage<>(Status.Failure, obj);
    }

    private final Status status;
    private final T cargo;

    private ResponseMessage(Status status, T cargo) {
        if (null == cargo) {
            throw new IllegalArgumentException("null");
        }
        this.status = status;
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        return String.format("{\"status\": \"%s\", \"cargo\": \"%s\"}", status, cargo);
    }
}
