package com.jappler.todo.resource;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.jappler.todo.dao.TodoDao;
import com.jappler.todo.model.Result;
import com.jappler.todo.model.SearchQuery;
import com.jappler.todo.model.Todo;

/**
 * Todo resource (exposed at "todo" path)
 */
@Path("todo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    @GET
    public List<Todo> getTodos() {
        return TodoDao.getInstance().getTodos();
    }
    
    @GET
    @Path("{id}")
    public Response getTodo(@PathParam("id") Long id) {
        Todo todo = TodoDao.getInstance().getTodo(id);
        
        return (todo != null) ? 
                Response.ok(todo).build() : 
                Response.status(Status.NOT_FOUND).build();
    }
    
    @GET
    @Path("{id}/complete")
    public Result completeTodo(@PathParam("id") Long id) {
        if (TodoDao.getInstance().completeTodo(id)) {
            return new Result(true, "Todo marked completed");
        } else {
            return new Result(false, "Couldn't mark todo complete");
        }
    }
    
    @PUT
    public Result updateTodo(Todo todo) {
        if (TodoDao.getInstance().updateTodo(todo)) {
            return new Result(true, "Update successful");
        } else {
            return new Result(false, "Error on update");
        }
    }
    
    @POST
    public Result createTodo(Todo todo) {
        if (TodoDao.getInstance().createTodo(todo)) {
            return new Result(true, "Create successful");
        } else {
            return new Result(false, "Error on create");
        }
    }
    
    @DELETE
    @Path("{id}")
    public Result deleteTodo(@PathParam("id") Long id) {
        if (TodoDao.getInstance().deleteTodo(id)) {
            return new Result(true, "Delete successful");
        } else {
            return new Result(false, "Error on delete");
        }
    }
    
    @POST
    @Path("search")
    public Set<Todo> searchTodos(SearchQuery query) {
        return TodoDao.getInstance().search(query.getQuery());
    }
}
