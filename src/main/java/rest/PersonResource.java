package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.Person;
import utils.EMF_Creator;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
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


//Todo Remove or change relevant parts before ACTUAL use
@Path("persons")
public class PersonResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory(
                "pu",
                "jdbc:mysql://localhost:3307/w5_03persons",
                "dev",
                "ax2",
                EMF_Creator.Strategy.CREATE);
    private static final PersonFacade FACADE =  PersonFacade.getPersonFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
            
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String demo() {
        return "{\"msg\":\"Hello World\"}";
    }
    
    @Path("/all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllPersons() {
        return new Gson().toJson(FACADE.getAllPersons());
    }
    
    @Path("/{id}") 
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPersonFromId(@PathParam("id") int id) {
        return new Gson().toJson(FACADE.getPerson(id));
    }
    
    @Path("/new")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addPerson(String person) {
        Person p = GSON.fromJson(person, Person.class); //manual conversion
        FACADE.addPerson(p.getFirstname(), p.getLastname(), p.getPhone());
        return Response.ok(p).build();
    }
    
    //Should be updated so that one cannot update the id
    @Path("/update/{id}")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public Response editPerson(String person) {
        Person p = GSON.fromJson(person, Person.class);
        FACADE.editPerson(p);
        return Response.ok(p).build();
    }
    
    @Path("/delete/{id}")
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    public Response deletePerson(@PathParam("id") int id) {
        Person p = FACADE.deletePerson(id);
        return Response.ok(p).build();
    }

}
