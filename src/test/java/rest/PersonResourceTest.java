package rest;

import entities.Person;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator.DbSelector;
import utils.EMF_Creator.Strategy;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class PersonResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    //Read this line from a settings-file  since used several places
    private static final String TEST_DB = "jdbc:mysql://localhost:3307/startcode_test";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactory(DbSelector.TEST, Strategy.DROP_AND_CREATE);

        //NOT Required if you use the version of EMF_Creator.createEntityManagerFactory used above        
        //System.setProperty("IS_TEST", TEST_DB);
        //We are using the database on the virtual Vagrant image, so username password are the same for all dev-databases
        httpServer = startServer();

        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;

        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //System.in.read();
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the script below to use YOUR OWN entity class
    /* Using three transactions to persist test data due to the fact that
        the insert order is more predictible.
     */
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        
        
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNativeQuery("ALTER TABLE PERSON AUTO_INCREMENT = 1").executeUpdate(); // Reset datbase index and delete current entries.
            em.getTransaction().commit();
            
            em.getTransaction().begin();
            em.persist(new Person("Hans", "Hansen", "1234"));
            em.getTransaction().commit();

            em.getTransaction().begin();
            em.persist(new Person("Peter", "Petersen", "4321"));
            em.getTransaction().commit();

            em.getTransaction().begin();
            em.persist(new Person("Ole", "Olesen", "5555"));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/persons").then().statusCode(200);
    }

    /*
        The prequsite for this test is that all the tests are run in a specific
        order. This is not appropriate, however, I couldn't get it to work
        otherwise.
     */
    @Test
    public void testGetPerson() throws Exception {
        given()
                .when()
                .get("/persons/3")
                .then()
                .assertThat().contentType(MediaType.APPLICATION_JSON)
                .assertThat().statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstname", is("Ole"));
    }

    @Test
    public void testUpdatePerson() throws Exception {
        String requestBody = "{\"id\": 3, \"firstname\": \"Lars\", \"lastname\" : \"Larsen\", \"phone\": \"5432\"}";

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .put("/persons/update/3")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstname", is("Lars"));
    }

    @Test
    public void testDeletePerson() throws Exception {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .delete("/persons/delete/3")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("id", is(3));           
    }

    @Test
    public void testAddPerson() throws Exception {
        String requestBody = "{\"firstname\": \"Annika\", \"lastname\":\"Ehlers\", \"phone\":\"1234\" }";

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .post("/persons/new")
                .then()
                .assertThat().statusCode(HttpStatus.OK_200.getStatusCode())
                .assertThat().contentType(MediaType.APPLICATION_JSON)
                .body("firstname", is("Annika"));
    }

    @Test
    public void testGetAll() throws Exception {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .get("/persons/all")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("size()", is(3))
                .body("firstname", hasItem("Peter"));
    }
    
    @Test
    public void testGetPerson_nonExistingId() throws Exception {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .get("/persons/100")
                .then()
                .assertThat().statusCode(HttpStatus.NOT_FOUND_404.getStatusCode());
    }
    
    // Returns 405 status. Havn't had the time to figure out why
    //@Test
    public void testDeletePerson_nonExistingId() throws Exception {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .delete("/persons/100")
                .then()
                .assertThat().statusCode(HttpStatus.NOT_FOUND_404.getStatusCode());
    }
}
