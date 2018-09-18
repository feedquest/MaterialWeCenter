package com.langtaosha.sjwyd;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.langtaosha.sjwyd.models.Action;
import com.langtaosha.sjwyd.models.Ajax;
import com.langtaosha.sjwyd.models.AnswerComment;
import com.langtaosha.sjwyd.models.AnswerDetail;
import com.langtaosha.sjwyd.models.Article;
import com.langtaosha.sjwyd.models.Chat;
import com.langtaosha.sjwyd.models.Conversation;
import com.langtaosha.sjwyd.models.Dynamic;
import com.langtaosha.sjwyd.models.ExploreItem;
import com.langtaosha.sjwyd.models.LoginProcess;
import com.langtaosha.sjwyd.models.PublishAnswer;
import com.langtaosha.sjwyd.models.PublishQuestion;
import com.langtaosha.sjwyd.models.QuestionDetail;
import com.langtaosha.sjwyd.models.Response;
import com.langtaosha.sjwyd.models.Responses;
import com.langtaosha.sjwyd.models.SearchResult;
import com.langtaosha.sjwyd.models.Topic;
import com.langtaosha.sjwyd.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Client {

    public static int PUBLISH = 101;
    public static int ANSWER = 201;
    private static Client client;
    private String cooike;

    /**
     * 获得一个实例
     *
     * @return Client对象
     */
    public static Client getInstance() {
        if (client == null)
            client = new Client();
        return client;
    }

    public @NonNull
    <T> Response<T> parseResponse(String json, Class<T> type) {
        Response<T> response = new Response<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            response.setErrno(jsonObject.getInt("errno"));
            response.setErr(jsonObject.getString("err"));
            if (response.getErrno() == 1 && !jsonObject.isNull("rsm")) {
                Gson gson = new Gson();
                response.setRsm(gson.fromJson(jsonObject.getJSONObject("rsm").toString(), type));
            } else {
                response.setRsm(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setErr("未知错误");
            response.setErrno(-1);
        }
        return response;
    }

    private <T> Responses<T> parseResponses(String json, @NonNull Class<T> classType) {
        Responses<T> responses = new Responses<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            responses.setErrno(jsonObject.getInt("errno"));
            responses.setErr(jsonObject.getString("err"));
            if (responses.getErrno() == 1) {
                ArrayList<T> list = new ArrayList<>();
                JSONObject rsm = jsonObject.getJSONObject("rsm");
                JSONArray array = rsm.getJSONArray("rows");
                int total_rows = rsm.getInt("total_rows");
                Gson gson = new Gson();
                for (int i = 0; i < total_rows; i++) {
                    JSONObject item = array.getJSONObject(i);
                    list.add(gson.fromJson(item.toString(), classType));
                }
                responses.setRsm(list);
            } else {
                responses.setRsm(null);
            }
        } catch (JSONException e) {
            responses.setRsm(null);
            responses.setErrno(-1);
            responses.setErr("未知错误");
        }
        return responses;
    }

    public String getSign(String apis) {
        if (Config.KEEP_SECRET) {
            String text = apis + Config.APP_SECRET;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                String cipher = byteArrayToHex(md.digest(text.getBytes()));
                Log.d("sign", cipher);
                return cipher;
            } catch (NoSuchAlgorithmException e) {
                return "";
            }
        } else {
            return "";
        }
    }


    public static String byteArrayToHex(byte[] byteArray) {
        // 首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    /**
     * loginProcess 用户登录
     *
     * @param user_name 用户名
     * @param password  用户密码
     * @return 包含LoginProcess的Result对象
     */
    public @NonNull
    Response<LoginProcess> loginProcess(@NonNull String user_name, @NonNull String password) {
        Map<String, String> params = new HashMap<>();
        params.put("user_name", user_name);
        params.put("password", password);
        String json = apiPost(Config.API_CAT_ACCOUNT, Config.API_LOGIN_PROCESS, params);
        return parseResponse(json, LoginProcess.class);
    }

    public @NonNull
    Response<LoginProcess> registerProcess(@NonNull String userName, @NonNull String password, @NonNull String email, @NonNull String iCode) {
        Map<String, String> params = new HashMap<>();
        params.put("user_name", userName);
        params.put("password", password);
        params.put("email", email);
        params.put("icode", iCode);
        String json = apiPost(Config.API_CAT_ACCOUNT, Config.API_REGISTER_PROCESS, params);
        return parseResponse(json, LoginProcess.class);
    }

    /**
     * getUserInfo 获取用户信息
     *
     * @param uid 用户ID
     * @return 包含UserInfo的Result对象
     */
    public Response<UserInfo> getUserInfo(long uid) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", String.valueOf(uid));
        String json = apiGet(Config.API_CAT_ACCOUNT, Config.API_GET_USERINFO, params);
        return parseResponse(json, UserInfo.class);
    }

    /**
     * getUserActions获取用户回答或提问记录
     *
     * @param uid     用户ID
     * @param actions 101-获取用户提问列表 201-获取用户回答列表
     * @return 包含Action对象数组的Result对象
     */
    public Responses<Action> getUserActions(long uid, int actions, int page) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", String.valueOf(uid));
        params.put("actions", String.valueOf(actions));
        params.put("page", String.valueOf(page));
        String json = apiGet(Config.API_CAT_PEOPLE, Config.API_USER_ACTIONS, params);
        return parseResponses(json, Action.class);
    }

    /**
     * explore 发现页面
     *
     * @param page 页数
     * @return Result对象
     */
    public Responses<ExploreItem> explore(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(Config.ITEM_PER_PAGE));
        String json = apiGet(Config.API_CAT_EXPLORE, params);
        return parseResponses(json, ExploreItem.class);
    }

    /**
     * getQuestion 获取问题详情
     *
     * @param id 问题的编号
     * @return Result对象
     */
    public Response<QuestionDetail> getQuestion(int id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        String json = apiGet(Config.API_CAT_QUESTION, "", params);
        return parseResponse(json, QuestionDetail.class);
    }

    public Response<Article> getArticle(int id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        String json = apiGet(Config.API_CAT_ARTICLE, params);
        return parseResponse(json, Article.class);
    }

    public Response<Object> saveComment(int article_id, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("article_id", String.valueOf(article_id));
        params.put("message", message);
        String json = apiPost(Config.API_CAT_ARTICLE, Config.API_SAVE_COMMENT, params);
        return parseResponse(json, Object.class);
    }

    /**
     * getAnswer 获取答案详情
     *
     * @param answer_id 答案的编号
     * @return Result对象
     */
    public Response<AnswerDetail> getAnswer(int answer_id) {
        Map<String, String> params = new HashMap<>();
        params.put("answer_id", String.valueOf(answer_id));
        String json = apiGet(Config.API_CAT_QUESTION, Config.API_ANSWER, params);
        return parseResponse(json, AnswerDetail.class);
    }

    /**
     * getAnswer 获取答案评论列表
     *
     * @param answer_id 答案的编号
     * @return Result对象
     */
    public Responses<AnswerComment> getAnswerComments(int answer_id) {
        // 请求
        Map<String, String> params = new HashMap<>();
        params.put("answer_id", String.valueOf(answer_id));
        String json = apiGet(Config.API_CAT_QUESTION, Config.API_ANSWER_COMMENTS, params);
        // 解析
        Responses<AnswerComment> responses = new Responses<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            responses.setErrno(jsonObject.getInt("errno"));
            responses.setErr(jsonObject.getString("err"));
            if (responses.getErrno() == 1) {
                ArrayList<AnswerComment> list = new ArrayList<>();
                JSONArray array = jsonObject.getJSONArray("rsm");
                Gson gson = new Gson();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    list.add(gson.fromJson(item.toString(), AnswerComment.class));
                }
                responses.setRsm(list);
            } else {
                responses.setRsm(null);
            }
        } catch (JSONException e) {
            responses.setRsm(null);
            responses.setErrno(-1);
            responses.setErr("未知错误");
        }
        return responses;
    }

    /**
     * getDynamic 首页动态（home）页面
     *
     * @return Result对象
     */
    public Responses<Dynamic> getDynamic(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        String json = apiGet(Config.API_CAT_HOME, "", params);
        return parseResponses(json, Dynamic.class);
    }

    /**
     * publishQuestion 发起问题
     *
     * @param content 问题的标题
     * @param detail  问题的内容
     * @param topics  问题的话题
     * @return 包含PublishQuestion对象的Result对象
     */
    public Response<PublishQuestion> publishQuestion(String content, String detail, ArrayList<String> topics) {
        Map<String, String> params = new HashMap<>();
        params.put("question_content", content);
        params.put("question_detail", detail);
        // 生成话题列表
        StringBuilder topic = new StringBuilder();
        if (!topics.isEmpty()) {
            topic.append(topics.get(0));
            for (int i = 1; i < topics.size(); i++)
                topic.append(',').append(topics.get(i));
        }
        params.put("topics", topic.toString());
        String json = apiPost(Config.API_CAT_PUBLISH, Config.API_PUBLISH_QUESTION, params);
        return parseResponse(json, PublishQuestion.class);
    }

    /**
     * publishAnswer 添加问题答案
     *
     * @param questionID 问题编号
     * @param content    答案的内容
     * @return 包含PublishAnswer对象的Result对象
     */

    public Response<PublishAnswer> publishAnswer(int questionID, String content) {
        Map<String, String> params = new HashMap<>();
        params.put("question_id", questionID + "");
        params.put("answer_content", content);
        String json = apiPost(Config.API_CAT_PUBLISH, Config.API_PUBLISH_ANSWER, params);
        return parseResponse(json, PublishAnswer.class);
    }

    public Response<Ajax> follow(int uid) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", String.valueOf(uid));
        String json = ajax(Config.AJAX_FOLLOW_PEOPLE, params);
        return parseResponse(json, Ajax.class);
    }

    /**
     * postAction 对问题或答案进行感谢、点赞等动作
     *
     * @param type      动作的类型
     * @param classType 类类型
     * @param strs      动作所需参的数列表
     * @return Result2（如果有错误，返回NULL）
     */

    public <T> Response<T> postAction(Config.ActionType type, @NonNull Class<T> classType, ArrayList<String> strs) {
        Map<String, String> params = new HashMap<>();
        String json;
        if (type == Config.ActionType.QUESTION_FOCUS) {
            params.put("question_id", strs.get(0));
            json = ajax(Config.AJAX_QUESTION_FOCUS, params);
            return parseResponse(json, classType);
        } else if (type == Config.ActionType.QUESTION_THANKS) {
            params.put("question_id", strs.get(0));
            json = ajax(Config.AJAX_QUESTION_THANKS, params);
            return parseResponse(json, classType);
        } else if (type == Config.ActionType.PUBLISH_ANSWER_COMMENT) {
            params.put("answer_id", strs.get(0));
            params.put("message", strs.get(1));
            json = apiPost(Config.API_CAT_QUESTION, Config.API_PUBLISH_ANSWER_COMMENT, params);
            return parseResponse(json, classType);
        } else if (type == Config.ActionType.ANSWER_VOTE) {
            params.put("answer_id", strs.get(0));
            params.put("value", strs.get(1));
            json = ajax(Config.AJAX_ANSWER_VOTE, params);
            return parseResponse(json, classType);
        } else if (type == Config.ActionType.ANSWER_RATE) {
            params.put("type", strs.get(0));
            params.put("answer_id", strs.get(1));
            json = ajax(Config.AJAX_ANSWER_RATE, params);
            return parseResponse(json, classType);
        } else if (type == Config.ActionType.ARTICLE_VOTE) {
            params.put("type", strs.get(0));
            params.put("item_id", strs.get(1));
            params.put("rating", strs.get(2));
            json = ajax(Config.AJAX_ARTICLE_VOTE, params);
            return parseResponse(json, classType);
        }
        return null;
    }

    public Response<Ajax> focus(long questionID) {
        Map<String, String> params = new HashMap<>();
        params.put("question_id", String.valueOf(questionID));
        String json = ajax(Config.AJAX_QUESTION_FOCUS, params);
        return parseResponse(json, Ajax.class);
    }

    public Response<Ajax> thanks(long questionID) {
        Map<String, String> params = new HashMap<>();
        params.put("question_id", String.valueOf(questionID));
        String json = ajax(Config.AJAX_QUESTION_THANKS, params);
        return parseResponse(json, Ajax.class);
    }

    public Response<Ajax> articleVote(int id, int rating) {
        Map<String, String> params = new HashMap<>();
        params.put("type", "article");
        params.put("item_id", String.valueOf(id));
        params.put("rating", String.valueOf(rating));
        String json = ajax(Config.AJAX_ARTICLE_VOTE, params);
        return parseResponse(json, Ajax.class);
    }

    public Response<Ajax> articleCommentVote(int id, int rating) {
        Map<String, String> params = new HashMap<>();
        params.put("type", "comment");
        params.put("item_id", String.valueOf(id));
        params.put("rating", String.valueOf(rating));
        String json = ajax(Config.AJAX_ARTICLE_VOTE, params);
        return parseResponse(json, Ajax.class);
    }

    public Response<Ajax> articleSaveComment(int id, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("article_id", String.valueOf(id));
        params.put("message", message);
        String json = apiPost(Config.API_CAT_ARTICLE, Config.API_SAVE_COMMENT, params);
        return parseResponse(json, Ajax.class);
    }

    public Responses<Topic> hotTopics(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        String json = apiGet(Config.API_CAT_TOPIC, Config.API_HOT_TOPICS, params);
        return parseResponses(json, Topic.class);
    }

    public Responses<Conversation> inbox() {
        Map<String, String> params = new HashMap<>();
        String json = apiGet(Config.API_CAT_INBOX, params);
        return parseResponses(json, Conversation.class);
    }

    public Responses<Chat> read(int id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        String json = apiGet(Config.API_CAT_INBOX, Config.API_READ, params);
        return parseResponses(json, Chat.class);
    }

    public Responses<SearchResult> search(String q) {
        Map<String, String> params = new HashMap<>();
        params.put("q", q);
        String json = apiGet(Config.API_CAT_SEARCH, params);
        return parseResponses(json, SearchResult.class);
    }

    public Response<Object> send(String message, String recipient) {
        Map<String, String> params = new HashMap<>();
        params.put("message", message);
        params.put("recipient", recipient);
        String json = apiPost(Config.API_CAT_INBOX, Config.API_SEND, params);
        return parseResponse(json, Object.class);
    }

    public Responses<ExploreItem> posts(int topicID, int page) {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(topicID));
        params.put("page", String.valueOf(page));
        String json = apiGet(Config.API_CAT_TOPIC, Config.API_POSTS, params);
        return parseResponses(json, ExploreItem.class);
    }

    private String ajax(String url, Map<String, String> params) {
        return doPost(Config.HOST_NAME + url, params);
    }

    private String apiPost(String apiCat, Map<String, String> params) {
        return apiPost(apiCat, "", params);
    }

    private String apiPost(String apiCat, String api, Map<String, String> params) {
        // 组合链接
        String apiUrl = Config.API_ROOT + apiCat + '/' + api + '/';
        if (Config.KEEP_SECRET) {
            apiUrl += "?mobile_sign=" + getSign(apiCat);
        }
        return doPost(apiUrl, params);
    }

    private String doPost(String apiUrl, Map<String, String> params) {
        Log.d("POST REQUEST", apiUrl);
        // 建立请求内容
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue()))
                    .append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        byte[] data = builder.toString().getBytes();
        // 发出请求
        try {
            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(Config.TIME_OUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            // 附上Cookie
            connection.setRequestProperty("Cookie", cooike);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 发送请求内容
            OutputStream output = connection.getOutputStream();
            output.write(data);
            // 接收返回信息
            int response = connection.getResponseCode();
            if (response == HttpsURLConnection.HTTP_OK) {
                // 保存Cookie
                Map<String, List<String>> header = connection.getHeaderFields();
                List<String> cookies = header.get("Set-Cookie");
                if (cookies.size() == 3)
                    cooike = cookies.get(2);
                // 处理返回的字符串流
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[Config.MAX_LINE_BUFFER];
                int len = 0;
                while ((len = input.read(buffer)) != -1)
                    byteArrayOutputStream.write(buffer, 0, len);
                String str = new String(byteArrayOutputStream.toByteArray());
                Log.d("POST RESPONSE", str);
                return str;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }


    private String apiGet(String apiCat, Map<String, String> params) {
        return apiGet(apiCat, "", params);
    }

    private String apiGet(String apiCat, String api, Map<String, String> params) {
        // 组合链接
        StringBuilder builder = new StringBuilder();
        builder.append(Config.API_ROOT);
        builder.append(apiCat);
        builder.append('/');
        builder.append(api);
        builder.append("/?");
        if (Config.KEEP_SECRET) {
            params.put("mobile_sign", getSign(apiCat));
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue()))
                    .append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return doGet(builder.toString());
    }

    private String doGet(String apiUrl) {
        try {
            // 组合链接
            Log.d("GET REQUEST", apiUrl);
            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            // 附上Cookie
            connection.setRequestProperty("Cookie", cooike);
            InputStreamReader input = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(input);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            String str = builder.toString();
            Log.d("GET RESPONSE", str);
            return str;
        } catch (IOException e) {
            return null;
        }
    }

}
