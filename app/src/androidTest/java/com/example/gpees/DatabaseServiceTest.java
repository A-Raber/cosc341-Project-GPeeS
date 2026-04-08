package com.example.gpees;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseServiceTest {

    private interface SeedCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private static class SeedBathroomData {
        private final Bathroom bathroom;
        private final List<Review> reviews;
        private final List<Comment> comments;

        private SeedBathroomData(Bathroom bathroom, List<Review> reviews, List<Comment> comments) {
            this.bathroom = bathroom;
            this.reviews = reviews;
            this.comments = comments;
            
            // Ensure bathroom starts with 0 rating so addReview transaction can calculate it correctly
            this.bathroom.setRating(0.0f);
            this.bathroom.setReviewCount(0);
        }
    }

    private DatabaseService databaseHelper;

    // A bathroomId we can reuse across tests
    private static String testBathroomId;

    @Before
    public void setUp() {
        databaseHelper = new DatabaseService();
    }

    // Add Bathroom
    @Test
    public void a_testAddBathroom() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Bathroom bathroom = new Bathroom(
                "Test Bathroom",
                "123 Test St",
                49.8801,
                -119.4436,
                Arrays.asList("accessible")
        );

        databaseHelper.addBathroom(bathroom, new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                testBathroomId = bathroom.getId();
                assertNotNull("Bathroom ID should not be null after save", testBathroomId);
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("addBathroom failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Get Bathrooms Nearby
    @Test
    public void c_testGetBathroomsNearby() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        // Search around Kelowna downtown
        double lat = 49.888;
        double lng = -119.496;
        double radius = 5000; // 5km

        databaseHelper.getBathroomsNearby(lat, lng, radius, new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                assertNotNull("Result should not be null", bathrooms);
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getBathroomsNearby failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Add Review
    @Test
    public void d_testAddReview() {
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Review review = new Review("testuser", 4.0f, "Pretty clean!", new Date());

        databaseHelper.addReview(testBathroomId, review, new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("addReview failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Seed Kelowna Data (Run this to populate your map for testing)
    @Test
    public void z_seedKelownaBathrooms() {
        List<SeedBathroomData> seedData = Arrays.asList(
            new SeedBathroomData(
                new Bathroom("City Park Washroom", "1600 Abbott St", 49.8841, -119.4978, Arrays.asList("accessible", "safe")),
                Arrays.asList(
                    new Review("kelownalocal", 4.5f, "Usually stocked and close to the beach.", new Date()),
                    new Review("morningwalker", 4.0f, "Clean early in the day.", new Date())
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Best option when you're already in City Park.", new Date()),
                    new Comment("beachrunner", "Line gets longer in the afternoon.", new Date())
                )
            ),
            new SeedBathroomData(
                new Bathroom("Waterfront Park", "1200 Water St", 49.8925, -119.4975, Arrays.asList("safe")),
                Arrays.asList(
                    new Review("tourist22", 3.5f, "Convenient but can get busy on weekends.", new Date()),
                    new Review("lakeview", 4.0f, "Good stop while walking the boardwalk.", new Date())
                ),
                Arrays.asList(
                    new Comment("dockside", "Lighting is decent after sunset.", new Date()),
                    new Comment("Anonymous", "Bring your own sanitizer just in case.", new Date())
                )
            ),
            new SeedBathroomData(
                new Bathroom("Downtown Paid Toilet", "Bernard Ave", 49.8872, -119.4961, Arrays.asList("cost", "accessible")),
                Arrays.asList(
                    new Review("budgettraveler", 2.5f, "Fine in an emergency, but paying is annoying.", new Date()),
                    new Review("wheelsonroad", 4.0f, "Easy accessible entry and enough space.", new Date())
                ),
                Arrays.asList(
                    new Comment("downtowncommuter", "Card reader worked for me.", new Date()),
                    new Comment("Anonymous", "Has been cleaner lately.", new Date())
                )
            ),
            new SeedBathroomData(
                new Bathroom("Gyro Beach Washroom", "3400 Lakeshore Rd", 49.8520, -119.4895, Arrays.asList("accessible", "clean")),
                Arrays.asList(
                    new Review("sunsetswim", 5.0f, "Surprisingly clean for a beach washroom.", new Date()),
                    new Review("familyday", 4.5f, "Spacious and easy to find.", new Date())
                ),
                Arrays.asList(
                    new Comment("parentmode", "Good stop if you have kids with you.", new Date()),
                    new Comment("lakeshorelocal", "Closed briefly one morning for cleaning.", new Date())
                )
            ),
            new SeedBathroomData(
                new Bathroom("Orchard Park Mall", "2271 Harvey Ave", 49.8828, -119.4428, Arrays.asList("safe", "clean")),
                Arrays.asList(
                    new Review("shopbreak", 4.5f, "Reliable and usually very clean.", new Date(1742947200000L)),
                    new Review("mallhopper", 4.0f, "Easy to access during store hours.", new Date(1743465600000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Near the food court entrance.", new Date(1743724800000L)),
                    new Comment("weekenderrand", "Busy at lunch but still manageable.", new Date(1744243200000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Mission Rec", "4105 Gordon Dr", 49.8348, -119.4801, Arrays.asList("clean", "accessible", "free")),
                Arrays.asList(
                    new Review("southkelowna", 4.5f, "Very clean during afternoon programs.", new Date(1744329600000L)),
                    new Review("pickleballfan", 4.0f, "Easy to find near the lobby.", new Date(1744416000000L))
                ),
                Arrays.asList(
                    new Comment("missionmom", "Good stop before heading to the fields.", new Date(1744502400000L)),
                    new Comment("Anonymous", "Doors were wide and easy to use.", new Date(1744588800000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Ben Lee Park", "900 Houghton Rd", 49.8876, -119.3908, Arrays.asList("free", "safe")),
                Arrays.asList(
                    new Review("rutlandrider", 3.5f, "Basic setup but usually open when needed.", new Date(1744675200000L)),
                    new Review("dogwalker7", 4.0f, "Better kept than I expected for a park washroom.", new Date(1744761600000L))
                ),
                Arrays.asList(
                    new Comment("playgrounddad", "Closest one to the spray park area.", new Date(1744848000000L)),
                    new Comment("Anonymous", "Lighting around it felt fine before dusk.", new Date(1744934400000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Knox Mountain", "450 Knox Mountain Dr", 49.911, -119.482, Arrays.asList("free", "safe")),
                Arrays.asList(
                    new Review("hiketrails", 4.0f, "A useful stop before going up the main trail.", new Date(1745020800000L)),
                    new Review("summitseekr", 3.5f, "Not fancy, but it gets the job done.", new Date(1745107200000L))
                ),
                Arrays.asList(
                    new Comment("trailcoffee", "Bring tissue on busy sunny weekends.", new Date(1745193600000L)),
                    new Comment("Anonymous", "Great location right by the trail.", new Date(1745280000000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Pandosy Village", "2900 Pandosy St", 49.8656, -119.4837, Arrays.asList("cost", "clean", "accessible")),
                Arrays.asList(
                    new Review("shoplocal", 4.0f, "Clean and central if you are walking the strip.", new Date(1745366400000L)),
                    new Review("mobilitymatters", 4.5f, "Accessible stall had enough turning space.", new Date(1745452800000L))
                ),
                Arrays.asList(
                    new Comment("cafehopper", "I paid with tap and it worked right away.", new Date(1745539200000L)),
                    new Comment("Anonymous", "Would like longer evening hours.", new Date(1745625600000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Rutland Library", "470 Gray Rd", 49.8889, -119.3964, Arrays.asList("accessible", "safe", "free")),
                Arrays.asList(
                    new Review("bookdrop", 4.5f, "Quiet, clean, and reliable during library hours.", new Date(1745712000000L)),
                    new Review("studybreak", 4.0f, "Staff area nearby makes it feel safer.", new Date(1745798400000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Best option in Rutland on a weekday.", new Date(1745884800000L)),
                    new Comment("readerlane", "Closed exactly when the library closed.", new Date(1745971200000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Kinsmen Park", "2600 Abbott St", 49.8724, -119.4888, Arrays.asList("clean", "free")),
                Arrays.asList(
                    new Review("rollerskater", 4.0f, "Usually clean in the morning.", new Date(1746057600000L)),
                    new Review("lakesideloop", 3.5f, "Close to the path and easy to spot.", new Date(1746144000000L))
                ),
                Arrays.asList(
                    new Comment("parklap", "Handy if you are jogging the waterfront route.", new Date(1746230400000L)),
                    new Comment("Anonymous", "Can get crowded after school lets out.", new Date(1746316800000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Stuart Park", "1430 Water St", 49.8911, -119.4968, Arrays.asList("safe", "free", "accessible")),
                Arrays.asList(
                    new Review("iceskater", 4.0f, "Convenient year-round downtown option.", new Date(1746403200000L)),
                    new Review("eventnight", 4.5f, "Stayed clean even during a busier evening.", new Date(1746489600000L))
                ),
                Arrays.asList(
                    new Comment("waterfrontwalk", "Close to the rink and public seating.", new Date(1746576000000L)),
                    new Comment("Anonymous", "Good lighting on my late walk back.", new Date(1746662400000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Capri Centre", "1835 Gordon Dr", 49.8802, -119.4765, Arrays.asList("clean", "safe", "cost")),
                Arrays.asList(
                    new Review("errandrun", 3.5f, "Not free, but usually cleaner than nearby options.", new Date(1746748800000L)),
                    new Review("quickstop88", 4.0f, "Easy stop while doing errands on Gordon.", new Date(1746835200000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Worth the small fee when downtown spots are full.", new Date(1746921600000L)),
                    new Comment("groceryloop", "Best accessed from the north entrance.", new Date(1747008000000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Okanagan Lake", "2100 Abbott St", 49.8768, -119.4897, Arrays.asList("cost", "clean")),
                Arrays.asList(
                    new Review("shoreline", 4.5f, "Cleaner than most lakefront washrooms.", new Date(1747094400000L)),
                    new Review("paddleboarder", 4.0f, "Great location before heading onto the beach.", new Date(1747180800000L))
                ),
                Arrays.asList(
                    new Comment("beachbag", "Nice breeze nearby but it can mean sand on the floor.", new Date(1747267200000L)),
                    new Comment("Anonymous", "Short walk from the main waterfront path.", new Date(1747353600000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Dilworth Plaza", "1980 Kane Rd", 49.8808, -119.4449, Arrays.asList("accessible", "safe", "free")),
                Arrays.asList(
                    new Review("midtownmeet", 4.0f, "Accessible and easy to reach from the parking lot.", new Date(1747440000000L)),
                    new Review("commuterkel", 3.5f, "Solid option when passing through Midtown.", new Date(1747526400000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Quiet location compared with the mall.", new Date(1747612800000L)),
                    new Comment("apptday", "Open during business hours when I went.", new Date(1747699200000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("UBCO Commons", "3333 University Way", 49.9406, -119.3959, Arrays.asList("clean", "accessible", "free")),
                Arrays.asList(
                    new Review("shopbreak", 4.5f, "Reliable and usually very clean.", new Date()),
                    new Review("mallhopper", 4.0f, "Easy to access during store hours.", new Date())
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Near the food court entrance.", new Date()),
                    new Comment("weekenderrand", "Busy at lunch but still manageable.", new Date())
                )
            )
        );

        for (SeedBathroomData seedBathroom : seedData) {
            CountDownLatch latch = new CountDownLatch(1);
            final String[] errorMessage = {null};

            databaseHelper.addBathroom(seedBathroom.bathroom, new DatabaseService.WriteCallback() {
                @Override
                public void onSuccess() {
                    seedReviewsThenComments(
                            seedBathroom.bathroom.getId(),
                            seedBathroom.reviews,
                            seedBathroom.comments,
                            new SeedCallback() {
                                @Override
                                public void onSuccess() {
                                    latch.countDown();
                                }

                                @Override
                                public void onFailure(String message) {
                                    errorMessage[0] = message;
                                    latch.countDown();
                                }
                            }
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    errorMessage[0] = "addBathroom seed failed: " + e.getMessage();
                    latch.countDown();
                }
            });

            awaitLatch(latch);
            if (errorMessage[0] != null) fail(errorMessage[0]);
        }
    }

    private void seedReviewsThenComments(String bathroomId, List<Review> reviews, List<Comment> comments, SeedCallback callback) {
        seedReviewAtIndex(bathroomId, reviews, 0, new SeedCallback() {
            @Override
            public void onSuccess() {
                seedCommentAtIndex(bathroomId, comments, 0, callback);
            }

            @Override
            public void onFailure(String message) {
                callback.onFailure(message);
            }
        });
    }

    private void seedReviewAtIndex(String bathroomId, List<Review> reviews, int index, SeedCallback callback) {
        if (index >= reviews.size()) {
            callback.onSuccess();
            return;
        }

        databaseHelper.addReview(bathroomId, reviews.get(index), new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                seedReviewAtIndex(bathroomId, reviews, index + 1, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure("addReview seed failed: " + e.getMessage());
            }
        });
    }

    private void seedCommentAtIndex(String bathroomId, List<Comment> comments, int index, SeedCallback callback) {
        if (index >= comments.size()) {
            callback.onSuccess();
            return;
        }

        databaseHelper.addComment(bathroomId, comments.get(index), new DatabaseService.WriteCallback() {
                @Override
                public void onSuccess() {
                    seedCommentAtIndex(bathroomId, comments, index + 1, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure("addComment seed failed: " + e.getMessage());
                }
            });
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) { fail("Test timed out waiting for Firebase response"); }
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}
