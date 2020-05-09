package run.halo.app.listener.post;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import run.halo.app.event.post.AbstractVisitEvent;
import run.halo.app.service.base.BasePostService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract visit event listener.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class AbstractVisitEventListener {

    private final Map<Integer, BlockingQueue<Integer>> visitQueueMap;

    private final Map<Integer, PostVisitTask> visitTaskMap;

    private final BasePostService basePostService;

    private final ExecutorService executor;

    protected AbstractVisitEventListener(BasePostService basePostService) {
        this.basePostService = basePostService;

        int initCapacity = 8;

        long count = basePostService.count();

        if (count < initCapacity) {
            initCapacity = (int) count;
        }

        visitQueueMap = new ConcurrentHashMap<>(initCapacity << 1);
        visitTaskMap = new ConcurrentHashMap<>(initCapacity << 1);

        this.executor = Executors.newCachedThreadPool();
    }

    private String findIp() {

        String ip = "";
        String chinaz = "http://ip.chinaz.com";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        Matcher m = p.matcher(inputLine.toString());
        if (m.find()) {
            String ipstr = m.group(1);
            ip = ipstr;
        }
        return ip;
    }

    private String findDistrict(String ip) {
        DatabaseReader reader;
        File database =  new File("C:\\Users\\84167\\Documents\\GitHub\\halo\\src\\GeoLite2-City.mmdb"); // 附件下载百度云地址https://pan.baidu.com/s/1ENqTeCoMIWJMbh88nYU5gg
        Country country = null;
        City city = null;
        try {
            reader = new DatabaseReader.Builder(database).build();
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            country = response.getCountry();
            city = response.getCity();
        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
        }
        assert country != null;
        String countryName = country.getName();
        return city.getName();
    }

    /**
     * Handle visit event.
     *
     * @param event visit event must not be null
     * @throws InterruptedException
     */
    protected void handleVisitEvent(@NonNull AbstractVisitEvent event) throws InterruptedException {
        Assert.notNull(event, "Visit event must not be null");

        // Get post id
        Integer id = event.getId();

        log.debug("Received a visit event, post id: [{}]", id);

        // Get post visit queue
        BlockingQueue<Integer> postVisitQueue = visitQueueMap.computeIfAbsent(id, this::createEmptyQueue);

        visitTaskMap.computeIfAbsent(id, this::createPostVisitTask);

        // Put a visit for the post
        postVisitQueue.put(id);
    }


    private PostVisitTask createPostVisitTask(Integer postId) {
        // Create new post visit task
        PostVisitTask postVisitTask = new PostVisitTask(postId);
        // Start a post visit task
        executor.execute(postVisitTask);

        log.debug("Created a new post visit task for post id: [{}]", postId);
        return postVisitTask;
    }

    private BlockingQueue<Integer> createEmptyQueue(Integer postId) {
        // Create a new queue
        return new LinkedBlockingQueue<>();
    }


    /**
     * Post visit task.
     */
    private class PostVisitTask implements Runnable {

        private final Integer id;

        private PostVisitTask(Integer id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BlockingQueue<Integer> postVisitQueue = visitQueueMap.get(id);
                    Integer postId = postVisitQueue.take();

                    log.debug("Took a new visit for post id: [{}]", postId);

                    // Increase the visit
                    basePostService.increaseVisit(postId);
<<<<<<< Updated upstream
=======
                    Visit visit = new Visit();
                    visit.setPostId(postId);
                    visit.setVisitId(visitId);
                    visit.setVisitorDistrict(findDistrict(findIp()));
                    visitService.create(visit);
                    visitId += 1;
>>>>>>> Stashed changes

                    log.debug("Increased visits for post id: [{}]", postId);
                } catch (InterruptedException e) {
                    log.debug("Post visit task: " + Thread.currentThread().getName() + " was interrupted", e);
                    // Ignore this exception
                }
            }

            log.debug("Thread: [{}] has been interrupted", Thread.currentThread().getName());
        }
    }
}
