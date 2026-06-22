package org.libertybikes.player.service;

import java.util.Collection;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.libertybikes.player.data.PlayerDB;

@Path("/player")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PlayerService {

    @Inject
    PlayerDB db;

    @Inject
    private JsonWebToken jwt;

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    MetricRegistry registry;

    private Counter getLoginCounter() {
        return registry.counter("num_player_logins");
    }

    @GET
    public Collection<Player> getPlayers() {
        return db.getAll();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public String createPlayer(@QueryParam("name") String name, @QueryParam("id") String id) {
        // Validate player name
        if (name == null)
            return null;
        name = name.replaceAll("[^\\x61-\\x7A\\x41-\\x5A\\x30-\\x39\\xC0-\\xFF -]", "").trim();
        if (name.length() == 0)
            return null;
        if (name.length() > 20)
            name = name.substring(0, 20);

        Player p = new Player(name, id);
        boolean isNewPlayer = db.create(p);
        
        if (isNewPlayer) {
            System.out.println("Created a new player with id=" + p.id);
        } else {
            System.out.println("A player already existed with id=" + p.id);
        }
        
        // Increment login counter for every login attempt (excluding sample players)
        if (!name.startsWith("SamplePlayer")) {
            getLoginCounter().inc();
        }

        return p.id;
    }

    @GET
    @Path("/{playerId}")
    public Player getPlayerById(@PathParam("playerId") String id) {
        if (id == null)
            return null;
        Player p = db.get(id);
        if (p == null)
            System.out.println("Unable to find any player with id=" + id);
        return p;
    }

    @GET
    @Path("/getJWTInfo")
    public HashMap<String, String> getJWTInfo() {

        HashMap<String, String> map = new HashMap<String, String>();

        String id = jwt.getClaim("id");
        if (db.exists(id)) {
            map.put("exists", "true");
            map.put("username", db.get(id).name);

        } else {
            map.put("exists", "false");
        }
        map.put("id", id);
        return map;
    }
}
