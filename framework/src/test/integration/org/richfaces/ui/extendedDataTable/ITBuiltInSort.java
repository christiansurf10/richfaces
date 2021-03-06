/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.ui.extendedDataTable;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;

import java.net.URL;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.richfaces.deployment.FrameworkDeployment;
import org.richfaces.shrinkwrap.descriptor.FaceletAsset;

@RunAsClient
@RunWith(Arquillian.class)
public class ITBuiltInSort {

    @Drone
    private WebDriver browser;

    @ArquillianResource
    private URL contextPath;

    @FindBy(id = "myForm:edt")
    private WebElement edt;

    @FindBy(id = "myForm:edt:0:n")
    private WebElement firstRow;

    @FindBy(id = "myForm:ajax")
    private WebElement button;

    @FindBy(id = "myForm:edt:header")
    private WebElement header;

    @FindBy(className = "rf-edt-srt")
    private WebElement sortHandle;

    @Deployment
    public static WebArchive createDeployment() {
        FrameworkDeployment deployment = new FrameworkDeployment(ITBuiltInSort.class);
        deployment.archive().addClass(IterationBuiltInBean.class);
        addIndexPage(deployment);

        return deployment.getFinalArchive();
    }

    @Test
    public void table_sort() throws InterruptedException {
        // given
        browser.get(contextPath.toExternalForm());
        WebElement cell = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt")).get(0);
        Assert.assertEquals("3", cell.getText());

        guardAjax(sortHandle).click();
        Thread.sleep(500);
        cell = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt")).get(0);
        Assert.assertEquals("0", cell.getText());

        guardAjax(sortHandle).click();
        Thread.sleep(500);
        cell = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt")).get(0);
        Assert.assertEquals("9", cell.getText());

        guardAjax(sortHandle).click();
        Thread.sleep(500);
        cell = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt")).get(0);
        Assert.assertEquals("0", cell.getText());

    }

    @Test
    public void table_filter() throws InterruptedException {
        // given
        browser.get(contextPath.toExternalForm());

        List<WebElement> cells = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt"));
        Assert.assertEquals("3", cells.get(0).getText());
        Assert.assertEquals(10, cells.size());

        WebElement filterInput = browser.findElement(By.id("myForm:edt:column2:flt"));
        filterInput.clear();
        filterInput.sendKeys("3");
        filterInput.sendKeys(Keys.TAB);
        Thread.sleep(500);
        cells = browser.findElements(By.cssSelector(".rf-edt-c-column2 .rf-edt-c-cnt"));
        Assert.assertEquals("3", cells.get(0).getText());
        Assert.assertEquals(4, cells.size());
    }

    private static void addIndexPage(FrameworkDeployment deployment) {
        FaceletAsset p = new FaceletAsset();

        p.body("<script type='text/javascript'>");
        p.body("function sortEdt(currentSortOrder) { ");
        p.body("  var edt = RichFaces.component('myForm:edt'); ");
        p.body("  var sortOrder = currentSortOrder == 'ascending' ? 'descending' : 'ascending'; ");
        p.body("  edt.sort('column2', sortOrder, true); ");
        p.body("} ");
        p.body("function filterEdt(filterValue) { ");
        p.body("  var edt = RichFaces.component('myForm:edt'); ");
        p.body("  edt.filter('column2', filterValue, true); ");
        p.body("} ");
        p.body("</script>");
        p.body("<h:form id='myForm'> ");
        p.body("    <r:extendedDataTable id='edt' value='#{iterationBuiltInBean.values}' var='bean' filterVar='fv' > ");
        p.body("        <r:column id='column1' width='150px' > ");
        p.body("            <f:facet name='header'>Column 1</f:facet> ");
        p.body("            <h:outputText value='Bean:' /> ");
        p.body("        </r:column> ");
        p.body("        <r:column id='column2' width='150px' ");
        p.body("                         sortBy='#{bean}' ");
        p.body("                         sortOrder='#{iterationBuiltInBean.sortOrder}' ");
        p.body("                         filterValue='#{iterationBuiltInBean.filterValue}' ");
        p.body("                         filterExpression='#{bean le fv}' > ");
        p.body("            <f:facet name='header'>Column 2</f:facet> ");
        p.body("            <h:outputText value='#{bean}' /> ");
        p.body("        </r:column> ");
        p.body("        <r:column id='column3' width='150px'" );
        p.body("                     sortBy='#{bean}' ");
        p.body("                     sortOrder='#{iterationBuiltInBean.sortOrder2}' > ");
        p.body("            <f:facet name='header'>Column 3</f:facet> ");
        p.body("            <h:outputText value='Row #{bean}, Column 3' /> ");
        p.body("        </r:column> ");
        p.body("    </r:extendedDataTable> ");
        p.body("    <r:commandButton id='ajax' execute='edt' render='edt' value='Ajax' /> ");
        p.body("</h:form> ");

        deployment.archive().addAsWebResource(p, "index.xhtml");
    }

}
