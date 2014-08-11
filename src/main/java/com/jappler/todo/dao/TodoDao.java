package com.jappler.todo.dao;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.ImmutableList;

import com.google.common.collect.Sets;
import com.jappler.todo.model.Todo;
import com.jappler.todo.search.SearchService;
import com.jappler.todo.search.SearchService.SearchIndex;

/**
 * Normally this would access a database, but
 * it uses an in-memory store for the time being
 * 
 * @author justinappler
 */
public class TodoDao {
    private static TodoDao instance;
    
    private List<Todo> todos;
    
    private SearchIndex<Todo> index;
    
    private TodoDao() {
        todos = new ArrayList<Todo>();
        index = SearchService.getInstance().getIndex(Todo.class, "todo", "todos");
        
        addSampleData(todos);
    }
    
    public static TodoDao getInstance() {
        if (instance == null) {
            instance = new TodoDao();
        }
        
        return instance;
    }
    
    /**
     * Adds some fake data that might exist in a persistent data store
     */
    private void addSampleData(List<Todo> todos) {
        Todo one = new Todo(1l, "Clean out the fridge", "Clean the shelves and throw away garbage", false);
        Todo two = new Todo(2l, "Do the dishes", "Wash each of the dishes with soap and water", false);
        Todo three = new Todo(3l, "Build a Jersey-based REST API for a hypothetical Todo app", "Write Java as needed", true);
        
        todos.add(one);
        index.index(one);
        
        todos.add(two);
        index.index(two);
        
        todos.add(three);
        index.index(three);
    }
    
    /**
     * Get all todo items
     */
    public List<Todo> getTodos() {
        return ImmutableList.copyOf(todos);
    }

    /**
     * Get a todo by id
     */
    public Todo getTodo(Long id) {
        if (id == null || id < 1) {
            return null;
        }
        
        for (Todo todo : todos) {
            if (todo.getId().equals(id)) {
                return todo;
            }
        }
        
        return null;
    }

    /**
     * Update a given todo item
     */
    public boolean updateTodo(Todo todo) {
        if (todos.contains(todo)) {
            todos.remove(todo);
            
            todos.add(todo);
            index.index(todo);
            return true;
        }
        
        return false;
    }

    /**
     * Create a new todo item
     */
    public boolean createTodo(Todo todo) {
        if (todo == null || todos.contains(todo)) {
            return false;
        }
        
        todos.add(todo);
        index.index(todo);
        
        return true;
    }

    /**
     * Delete a given todo item
     */
    public boolean deleteTodo(Long id) {
        if (getTodo(id) != null) {
            todos.remove(getTodo(id));
            return true;
        }
        
        return false;
    }

    /**
     * Mark a given todo item as completed
     */
    public boolean completeTodo(Long id) {
        if (getTodo(id) != null) {
            Todo todo = getTodo(id);
            
            if (todo.isDone()) {
                return false;
            } else {
                todo.setDone(true);
                index.index(todo);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Search for a given todo item using a simple query string
     */
    public Set<Todo> search(String query) {
       List<Todo> indexResults = index.query(query);
       
       if (indexResults != null && !indexResults.isEmpty()) {
           LinkedHashSet<Todo> results = Sets.newLinkedHashSet();
           
           for (Todo todo : indexResults) {
               if (todos.contains(todo)) {
                   results.add(getTodo(todo.getId()));
               }
           }

           return results;
       }
       
       return Sets.newHashSet();
    }
}
