package org.richfaces.ui.tabPanel;

import static org.jboss.arquillian.graphene.Graphene.guardXhr;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DynamicTabTestHelper {

    public void check_tab_switch(WebElement tabPanel, List<WebElement> tabs, WebElement a4jCreateTabButton) {
        Assert.assertEquals(6, tabs.size());
        Assert.assertEquals("content of tab 0", getTabContent(tabPanel).getText());

        guardXhr(tabs.get(2)).click();
        Assert.assertEquals("content of tab 2", getTabContent(tabPanel).getText());

        guardXhr(tabs.get(4)).click();
        Assert.assertEquals("content of tab 4", getTabContent(tabPanel).getText());

        guardXhr(tabs.get(5)).click();
        Assert.assertEquals("content of tab 5", getTabContent(tabPanel).getText());

        guardXhr(tabs.get(0)).click();
        Assert.assertEquals("content of tab 0", getTabContent(tabPanel).getText());

        guardXhr(a4jCreateTabButton).click();
        Assert.assertEquals(7, tabs.size());

        guardXhr(tabs.get(6)).click();
        Assert.assertEquals("content of tab 6", getTabContent(tabPanel).getText());

        guardXhr(tabs.get(0)).click();
        Assert.assertEquals("content of tab 0", getTabContent(tabPanel).getText());

        WebElement removeLink =tabs.get(6).findElement(By.tagName("a"));
        guardXhr(removeLink).click();
        Assert.assertEquals(6, tabs.size());
    }

    public WebElement getTabContent(WebElement tabPanel) {
        for (WebElement tabContent : tabPanel.findElements(By.className("rf-tab"))) {
            if (tabContent.isDisplayed()) {
                return tabContent;
            }
        }
        return null;
    }

    public void check_row_removal(WebElement tabPanel, List<WebElement> tabs, WebElement a4jCreateTabButton) {
        Assert.assertEquals(6, tabs.size());

        guardXhr(a4jCreateTabButton).click();
        guardXhr(a4jCreateTabButton).click();
        guardXhr(a4jCreateTabButton).click();

        Assert.assertEquals(9, tabs.size());

        WebElement removeLink =tabs.get(8).findElement(By.tagName("a"));
        guardXhr(removeLink).click();
        Assert.assertEquals(8, tabs.size());

        removeLink =tabs.get(7).findElement(By.tagName("a"));
        guardXhr(removeLink).click();
        Assert.assertEquals(7, tabs.size());

        removeLink =tabs.get(6).findElement(By.tagName("a"));
        guardXhr(removeLink).click();
        Assert.assertEquals(6, tabs.size());
    }
}
