package athena.crawler;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class PageRank {

    private String filePath;
    private ArrayList<String> sinkPages = new ArrayList<>();
    private List<Map.Entry<String, Double>> sortedPageRanks = new ArrayList<>();
    private HashMap<String, HashSet<String>> pageInLinks = new HashMap<>();
    private HashMap<String, HashSet<String>> pageOutLinks = new HashMap<>();
    private Integer documentSize = 0;

    private static final Integer ZERO = 0;
    private static final Double DAMPING_FACTOR = 0.85;
    private static final Double LOG2_TO_BASE_E = 0.693147180;

    public PageRank() {
    }

    public HashMap<String, HashSet<String>> getInLinkGraph(String folderPath) {
        HashMap<String, HashSet<String>> pageInLinks = new HashMap<>();
        try {
            File folder = new File(folderPath);
            File[] files = folder.listFiles();
            for (File file : files) {
                String documentTitle = StringUtils.remove(file.getName(), " - Wikipedia.html");
                pageInLinks.put(documentTitle.trim().replace(" ", "_"), new HashSet<>());
            }
            for (File file : files) {
                Document document = Jsoup.parse(file, "UTF-8");
                String documentTitle = StringUtils.remove(document.title(), " - Wikipedia");
                documentTitle = documentTitle.trim().replace(" ", "_");
                pageInLinks = parseAndCollectLinks(document, documentTitle, pageInLinks);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageInLinks;
    }

    private HashMap<String, HashSet<String>> parseAndCollectLinks(Document document, String documentID, HashMap<String, HashSet<String>> pageInLinks) {
        HashSet<String> inLinks;
        String elementUrl;
        String title;
        Element content = document.getElementById("mw-content-text");
        content.getElementsByAttributeValue("class", "references").remove();
        content.getElementsByAttributeValue("class", "citation book").remove();
        content.getElementsByAttributeValue("class", "navbox").remove();

        Elements elements = content.select("a[href]");
        for (Element element : elements) {
            elementUrl = element.attributes().get("href");
            if (elementUrl.startsWith("/wiki/") && !(elementUrl.contains("/wiki/Main_Page"))
                    && !(elementUrl.contains(":"))) {
                title = element.attributes().get("title");
                if (!title.isEmpty()) {
                    title = title.trim().replace(" ", "_");
                    if (pageInLinks.containsKey(title)) {
                        inLinks = pageInLinks.get(title);
                        inLinks.add(documentID);
                        pageInLinks.put(title, inLinks);
                    }
                }
            }
        }
        return pageInLinks;
    }

    public void processPageRank(String filePath) {
        this.filePath = filePath;
        setPageGraphFromFile();
        setDocumentSize();
        setSinkPages();
        this.sortedPageRanks = sortPageRank(evaluatePageRank());
    }

    public Integer getDocumentSize() {
        return documentSize;
    }

    private void setDocumentSize() {
        this.documentSize = pageInLinks.size();
    }

    public HashMap<String, HashSet<String>> getPageInLinks() {
        if (pageInLinks.size() == ZERO) {
            setPageGraphFromFile();
        }
        return pageInLinks;
    }

    public HashMap<String, HashSet<String>> getPageOutLinks() {
        if (pageOutLinks.size() == ZERO) {
            setPageGraphFromFile();
        }
        return pageOutLinks;
    }
    public void printInLink() {
        Set<String> hs = pageInLinks.keySet();
        HashMap<String, Integer> top = new HashMap<>();
        for (String doc: hs
             ) {
            top.put(doc, pageInLinks.get(doc).size());
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(top.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        System.out.println(entries);
    }

    public void createPageGraphFromOutLink(String folderPath) {
        HashMap<String, HashSet<String>> inLinksGraph = getInLinkGraph(folderPath);
        HashSet<String> inLinks;
        try {
            FileWriter fileWriter = new FileWriter(new File("ak_inlinks"));
            Set<String> documentSet = inLinksGraph.keySet();
            String str;
            for (String documentID : documentSet) {
                str = documentID;
                inLinks = inLinksGraph.get(str);
                for (String innerDocID : inLinks) {
                    str = str + " " + innerDocID;
                }
                fileWriter.write(str + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPageGraphFromFile() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String currentLine = bufferedReader.readLine();

            while (currentLine != null) {
                updatePageInLinks(currentLine);
                currentLine = bufferedReader.readLine();
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updatePageInLinks(String currentLine) {
        HashSet<String> inLinks = new HashSet<>();
        List<String> allLinks = new ArrayList<>();
        String[] stringsLinks = StringUtils.split(currentLine.trim(), " ");
        String newDocumentID = stringsLinks[ZERO];
        Collections.addAll(allLinks, stringsLinks);
        for (int i = 0; i < allLinks.size(); i++) {
            if (i == 0) {
                newDocumentID = allLinks.get(i);
            } else {
                inLinks.add(allLinks.get(i));
            }
        }
        pageInLinks.put(newDocumentID, inLinks);
        updatePageOutLinks(newDocumentID, inLinks);
    }

    private void updatePageOutLinks(String newDocumentID, HashSet<String> inLinks) {
        HashSet<String> outLinks;
        for (String documentID : inLinks) {
            if (pageOutLinks.containsKey(documentID)) {
                outLinks = pageOutLinks.get(documentID);
            } else {
                outLinks = new HashSet<>();
            }
            outLinks.add(newDocumentID);
            pageOutLinks.put(documentID, outLinks);
        }
    }


    private void setSinkPages() {
        Set<String> inLinkKeySet = getPageInLinks().keySet();
        for (String documentID : inLinkKeySet) {
            if (!pageOutLinks.containsKey(documentID)) {
                sinkPages.add(documentID);
            }
        }
    }

    private HashMap<String, Double> evaluatePageRank() {
        Set<String> pageGraphKeySet = pageInLinks.keySet();
        Double initialPageRankScore = (1.0 / documentSize);
        Integer convergenceCount = 0;
        Double lastPerplexity;
        Double currentPerplexity = 0.0;
        HashMap<String, Double> pageRankScores = new HashMap<>();
        for (String documentID : pageGraphKeySet) {
            pageRankScores.put(documentID, initialPageRankScore);
        }
        int iterationCount = 0;
        while (convergenceCount < 4) {
            Double sinkPageRank = 0.0;
            for (String documentID : sinkPages) {
                sinkPageRank = sinkPageRank + pageRankScores.get(documentID);
            }
            HashMap<String, Double> newPageRankScores = new HashMap<>();
            Double currentScore;
            Double initialScore = (1 - DAMPING_FACTOR) / documentSize;
            initialScore = initialScore + ((sinkPageRank * DAMPING_FACTOR) / documentSize);
            for (String documentID : pageGraphKeySet) {
                currentScore = initialScore;
                for (String inLinkPage : pageInLinks.get(documentID)) {
                    pageRankScores.putIfAbsent(inLinkPage, initialPageRankScore);
                    currentScore += (pageRankScores.get(inLinkPage) * DAMPING_FACTOR) / (pageOutLinks.get(inLinkPage).size());
                }
                newPageRankScores.put(documentID, currentScore);
            }
            pageRankScores = newPageRankScores;
            lastPerplexity = currentPerplexity;
            currentPerplexity = getPerplexity(pageRankScores);
            if ((currentPerplexity - lastPerplexity) < 1) {
                convergenceCount++;
            } else {
                convergenceCount = 0;
            }
            iterationCount++;
            System.out.println(currentPerplexity);
        }
        System.out.println("Convergence reached in " + iterationCount + " iterations");
        return pageRankScores;
    }

    public LinkedHashMap<String, Double> getPageRankWithScores() {
        LinkedHashMap<String, Double> sortedPageRank = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : sortedPageRanks) {
            sortedPageRank.put(e.getKey(), e.getValue());
        }
        return sortedPageRank;
    }

    public ArrayList<String> getPageRank() {
        ArrayList<String> sortedPageRank = new ArrayList<>();
        for (Map.Entry<String, Double> e : sortedPageRanks) {
            sortedPageRank.add(e.getKey());
        }
        return sortedPageRank;
    }

    private List<Map.Entry<String, Double>> sortPageRank(HashMap<String, Double> pageRankScores) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(pageRankScores.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return entries;
    }


    private double getPerplexity(HashMap<String, Double> pageRankScores) {
        return Math.pow(2, getEntropy(pageRankScores));
    }

    private double getEntropy(HashMap<String, Double> pageRankScores) {
        Set<String> scoresKeySet = pageRankScores.keySet();
        Double entropy = 0.0;
        Double score;
        for (String documentID : scoresKeySet) {
            score = pageRankScores.get(documentID);
            entropy += (score * (Math.log(score) / LOG2_TO_BASE_E));
        }
        return (-entropy);
    }

    public List<String> getSinkPages() {
        if (sinkPages.size() == ZERO) {
            setSinkPages();
        }
        return sinkPages;
    }

    public int getCountNoInLinkPages() {
        Set<String> stringSet = pageInLinks.keySet();
        int count = 0;
        for (String documentID : stringSet) {
            if(pageInLinks.get(documentID).size() == 0) {
                count++;
            }

        }
        return count;
    }

    public void printPageRanks(LinkedHashMap<String, Double> pageRanks, int size) {
        Set<String> keySet = pageRanks.keySet();
        System.out.println("Top " + size + " Pages");
        for (String documentID : keySet) {
            System.out.println(documentID + " : " + pageRanks.get(documentID));
            size--;
            if (size == 0) {
                break;
            }
        }
    }

    public void sumPageRanks(HashMap<String, Double> pageRanks) {
        Set<String> keySet = pageRanks.keySet();
        Double sum = 0.0;
        for (String documentID : keySet) {
            sum += pageRanks.get(documentID);
        }
        System.out.println("Total : " + sum);
    }
}
