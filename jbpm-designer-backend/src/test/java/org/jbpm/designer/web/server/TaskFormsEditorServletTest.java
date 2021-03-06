/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.designer.web.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.jbpm.designer.helper.TestHttpServletRequest;
import org.jbpm.designer.helper.TestHttpServletResponse;
import org.jbpm.designer.helper.TestServletConfig;
import org.jbpm.designer.helper.TestServletContext;
import org.jbpm.designer.repository.Asset;
import org.jbpm.designer.repository.AssetBuilderFactory;
import org.jbpm.designer.repository.Repository;
import org.jbpm.designer.repository.RepositoryBaseTest;
import org.jbpm.designer.repository.UriUtils;
import org.jbpm.designer.repository.filters.FilterByExtension;
import org.jbpm.designer.repository.impl.AssetBuilder;
import org.jbpm.designer.repository.vfs.VFSRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TaskFormsEditorServletTest  extends RepositoryBaseTest {

    @Before
    public void setup() {
        super.setup();
    }

    @After
    public void teardown() {
        super.teardown();
    }

    @Test
    public void testSaveFormAsset() throws Exception {
        Repository repository = new VFSRepository(producer.getIoService());
        ((VFSRepository)repository).setDescriptor(descriptor);
        profile.setRepository(repository);
        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("bpmn2 content")
                .type("bpmn2")
                .name("testprocess")
                .location("/defaultPackage");
        String uniqueId = repository.createAsset(builder.getAsset());
        // setup parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("uuid", uniqueId);
        params.put("action", "save");
        params.put("profile", "jbpm");
        params.put("taskname", Base64.encodeBase64String(UriUtils.encode("evaluate").getBytes()));
        params.put("tfvalue", "this is simple task content");
        params.put("formtype", "ftl");

        TaskFormsEditorServlet taskFormsEditorServlet = new TaskFormsEditorServlet();
        taskFormsEditorServlet.setProfile(profile);

        taskFormsEditorServlet.init(new TestServletConfig(new TestServletContext(repository)));

        taskFormsEditorServlet.doPost(new TestHttpServletRequest(params), new TestHttpServletResponse());

        Collection<Asset> forms = repository.listAssets("/defaultPackage", new FilterByExtension("ftl"));
        assertNotNull(forms);
        assertEquals(1, forms.size());
        Iterator<Asset> assets = forms.iterator();

        Asset asset1 = assets.next();
        assertEquals("evaluate-taskform", asset1.getName());
        assertEquals("/defaultPackage", asset1.getAssetLocation());

        Asset<String> form1 = repository.loadAsset(asset1.getUniqueId());
        assertNotNull(form1.getAssetContent());
        assertEquals("this is simple task content", form1.getAssetContent());

    }

    @Test
    public void testLoadFormAsset() throws Exception {
        Repository repository = new VFSRepository(producer.getIoService());
        ((VFSRepository)repository).setDescriptor(descriptor);
        profile.setRepository(repository);
        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("bpmn2 content")
                .type("bpmn2")
                .name("testprocess")
                .location("/defaultPackage");
        String uniqueId = repository.createAsset(builder.getAsset());

        AssetBuilder builderForm = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builderForm.content("this is simple task content")
                .type("ftl")
                .name("evaluate-taskform")
                .location("/defaultPackage");
        String uniqueIdForm = repository.createAsset(builderForm.getAsset());

        // setup parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("uuid", uniqueId);
        params.put("action", "load");
        params.put("profile", "jbpm");
        params.put("taskname", Base64.encodeBase64String(UriUtils.encode("evaluate").getBytes()));
        params.put("tfvalue", "this is simple task content");
        params.put("formtype", "ftl");

        TaskFormsEditorServlet taskFormsEditorServlet = new TaskFormsEditorServlet();
        taskFormsEditorServlet.setProfile(profile);

        taskFormsEditorServlet.init(new TestServletConfig(new TestServletContext(repository)));
        TestHttpServletResponse response = new TestHttpServletResponse();
        taskFormsEditorServlet.doPost(new TestHttpServletRequest(params), response);

        String formData =      new String(response.getContent());
        System.out.println(formData);
        assertEquals("this is simple task content", formData);
    }


    @Test
    public void testLoadForm_i18nName() throws Exception {

        Repository repository = new VFSRepository(producer.getIoService());
        ((VFSRepository)repository).setDescriptor(descriptor);
        profile.setRepository(repository);
        AssetBuilder builder = AssetBuilderFactory.getAssetBuilder(Asset.AssetType.Text);
        builder.content("bpmn2 content")
                .type("bpmn2")
                .name("BPTaskForm_i18nNames")
                .location("/defaultPackage");
        String uniqueId = repository.createAsset(builder.getAsset());

        // setup parameters
        String taskName = "проверить";
        Map<String, String> params = new HashMap<String, String>();
        params.put("uuid", uniqueId);
        params.put("action", "load");
        params.put("profile", "jbpm");
        params.put("taskname", Base64.encodeBase64String(UriUtils.encode(taskName).getBytes()));
        params.put("tfvalue", "this is simple task content");
        params.put("formtype", "ftl");

        TaskFormsEditorServlet taskFormsEditorServlet = new TaskFormsEditorServlet();
        taskFormsEditorServlet.setProfile(profile);

        taskFormsEditorServlet.init(new TestServletConfig(new TestServletContext(repository)));
        TestHttpServletResponse response = new TestHttpServletResponse();
        taskFormsEditorServlet.doPost(new TestHttpServletRequest(params), response);

        Collection<Asset> forms = repository.listAssets("/defaultPackage", new FilterByExtension("ftl"));
        assertNotNull(forms);
        assertEquals(1, forms.size());
        Iterator<Asset> assets = forms.iterator();
        Asset asset1 = assets.next();
        assertEquals(taskName + "-taskform", asset1.getName());
    }
}
