/*
 * Copyright (c) 2017 daloonik
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package kerguelenpetrel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rometools.rome.io.FeedException;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

@SuppressWarnings("serial")
public class FriendSomeoneServlet extends HttpServlet
   {
   public static final long cursor = -1;
   public static final String feedsFile = "WEB-INF/StaticFiles/feeds";
   public static final Random r = new Random();   
   
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
     {
     User friend = null;  
     resp.setContentType("text/plain; charset=UTF-8");
     try 
        {
        //Get the Twitter object
        Twitter twit = TwitterFactory.getSingleton();

        //Find a friend of a follower to bother
        long[] followerIDs = twit.getFollowersIDs(twit.getId(), cursor, 30).getIDs();
           
        if(followerIDs.length == 0)
           {
           resp.getWriter().println("Cannot find any followers \n");
           return;
           }
           
        //Load the potential victim IDs
        long[] friendIDs = twit.getFollowersIDs(followerIDs[r.nextInt(followerIDs.length)], cursor).getIDs();
       
        if(friendIDs.length == 0)
           {
           resp.getWriter().println("Cannot find any followers to bother \n");
           return;
           }
             
        //Get a new friend
        friend = twit.showUser(friendIDs[r.nextInt(friendIDs.length)]);
        twit.createFriendship(friend.getId());
        resp.getWriter().println("Made a new friend with @"+friend.getScreenName());
        
        //Write to our new friend
        StatusUpdate status = new StatusUpdate(writeToFriend(friend.getScreenName(), resp));
        twit.updateStatus(status);
        resp.getWriter().println("Tweet posted: "+ status.getStatus());
        
        //Retweet a random tweet from our new friend
        List<Status> tweets = twit.getUserTimeline(friendID); // Get the first 20 tweets of our new friend
        Status randomStatus = tweets.get(new Random().nextInt(tweets.size()));    
        twit.retweetStatus(randomStatus.getId()); 
        }
     catch(TwitterException e)
        {
        resp.getWriter().println("Problem with Twitter \n");
        e.printStackTrace(resp.getWriter());
        }
     }
   
     public String writeToFriend(String friendScreenName, HttpServletResponse resp) throws IOException
        {
        StringBuilder builder = new StringBuilder();       
        try
           {
           builder.append("@" + friendScreenName);
           builder.append(" ");
        
           //Append feed description
           GetFeed feed = new GetFeed(feedsFile); 
           builder.append(feed.description());
        
           if(builder.length() > 280) //Tweets are a maximum of 280 characters
             {
             builder.setLength(0);
             builder.append("@" + friendScreenName);
             builder.append(" hi!");
           }
         }
       catch(FeedException e)
          {
          resp.getWriter().println("Problem with RSS Feed \n");
          e.printStackTrace(resp.getWriter());
          }
        catch(FileNotFoundException e)
          {
          resp.getWriter().println("Input file(s) not found \n");
          e.printStackTrace(resp.getWriter());
          } 
       
       return builder.toString();
       }
    }
