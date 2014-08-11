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
    
    private void addSampleData(List<Todo> todos) {
        Todo one = new Todo(1l, "Clean out the fridge", "Clean the shelves and throw away garbage", false);
        Todo two = new Todo(2l, "Do the dishes", "Wash each of the dishes with soap and water", false);
        Todo three = new Todo(3l, "Build a Jersey-based REST API for a hypothetical Todo app", "Write Java as needed", false);
        
        todos.add(one);
        index.index(one);
        
        todos.add(two);
        index.index(two);
        
        todos.add(three);
        index.index(three);
    }
    
    public List<Todo> getTodos() {
        return ImmutableList.copyOf(todos);
    }

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

    public boolean updateTodo(Todo todo) {
        if (todos.contains(todo)) {
            todos.remove(todo);
            todos.add(todo);
            index.index(todo);
            return true;
        }
        
        return false;
    }

    public boolean createTodo(Todo todo) {
        if (todo == null || todos.contains(todo)) {
            return false;
        }
        
        todos.add(todo);
        index.index(todo);
        
        return true;
    }

    public boolean deleteTodo(Todo todo) {
        if (todos.contains(todo)) {
            todos.remove(todo);
            return true;
        }
        
        return false;
    }

    public boolean completeTodo(Long id) {
        if (id == null || id < 1) {
            return false;
        }
        
        for (Todo todo : todos) {
            if (todo.getId().equals(id)) {
                if (todo.isDone()) {
                    return false;
                } else {
                    todo.setDone(true);
                    index.index(todo);
                    return true;
                }
            }
        }
        
        return false;
    }

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